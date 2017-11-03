package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.domain.*;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.Response;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.zalando.fahrschein.JsonParserHelper.expectToken;
import static org.zalando.fahrschein.Preconditions.checkState;

class NakadiReader<T> implements IORunnable {

    private static final Logger LOG = LoggerFactory.getLogger(NakadiReader.class);
    private static final TypeReference<Collection<Cursor>> COLLECTION_OF_CURSORS = new TypeReference<Collection<Cursor>>() {
    };

    private final URI uri;
    private final RequestFactory requestFactory;
    private final BackoffStrategy backoffStrategy;
    private final CursorManager cursorManager;

    private final Set<String> eventNames;
    private final Optional<Subscription> subscription;
    private final Optional<Lock> lock;
    private final EventReader<T> eventReader;
    private final Listener<T> listener;
    private final BatchHandler batchHandler;

    private final JsonFactory jsonFactory;
    private final ObjectWriter cursorHeaderWriter;

    private final MetricsCollector metricsCollector;

    /*
     * @VisibleForTesting
     */
    NakadiReader(URI uri, RequestFactory requestFactory, BackoffStrategy backoffStrategy, CursorManager cursorManager, ObjectMapper objectMapper, Set<String> eventNames, Optional<Subscription> subscription, Optional<Lock> lock, Class<T> eventClass, Listener<T> listener) {
        this(uri, requestFactory, backoffStrategy, cursorManager, eventNames, subscription, lock, new MappingEventReader<>(eventClass, objectMapper), listener, DefaultBatchHandler.INSTANCE, NoMetricsCollector.NO_METRICS_COLLECTOR);
    }

    NakadiReader(URI uri, RequestFactory requestFactory, BackoffStrategy backoffStrategy, CursorManager cursorManager, Set<String> eventNames, Optional<Subscription> subscription, Optional<Lock> lock, EventReader<T> eventReader, Listener<T> listener, BatchHandler batchHandler, final MetricsCollector metricsCollector) {

        checkState(subscription.isPresent() || eventNames.size() == 1, "Low level api only supports reading from a single event");

        this.uri = uri;
        this.requestFactory = requestFactory;
        this.backoffStrategy = backoffStrategy;
        this.cursorManager = cursorManager;
        this.eventNames = eventNames;
        this.subscription = subscription;
        this.lock = lock;
        this.eventReader = eventReader;
        this.listener = listener;
        this.batchHandler = batchHandler;
        this.metricsCollector = metricsCollector;

        this.jsonFactory = DefaultObjectMapper.INSTANCE.getFactory();
        this.cursorHeaderWriter = DefaultObjectMapper.INSTANCE.writerFor(COLLECTION_OF_CURSORS);
    }

    static final class Batch<T> {
        private final Cursor cursor;
        private final List<T> events;

        Batch(Cursor cursor, List<T> events) {
            this.cursor = cursor;
            this.events = events;
        }

        Cursor getCursor() {
            return cursor;
        }

        List<T> getEvents() {
            return events;
        }
    }

    static class JsonInput implements Closeable {
        private final JsonFactory jsonFactory;
        private final Response response;
        private JsonParser jsonParser;

        JsonInput(JsonFactory jsonFactory, Response response) {
            this.jsonFactory = jsonFactory;
            this.response = response;
        }

        Response getResponse() {
            return response;
        }

        JsonParser getJsonParser() throws IOException {
            if (jsonParser == null) {
                jsonParser = jsonFactory.createParser(response.getBody()).disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            }
            return jsonParser;
        }

        @Override
        public void close() {
            try {
                if (jsonParser != null) {
                    try {
                        LOG.trace("Trying to close json parser");
                        jsonParser.close();
                        LOG.trace("Closed json parser");
                    } catch (IOException e) {
                        LOG.warn("Could not close json parser", e);
                    }
                }
            } finally {
                LOG.trace("Trying to close response");
                response.close();
                LOG.trace("Closed response");
            }
        }
    }

    private static Optional<String> getStreamId(Response response) {
        final Headers headers = response.getHeaders();
        final String streamId = headers == null ? null : headers.getFirst("X-Nakadi-StreamId");
        return Optional.ofNullable(streamId);
    }

    private JsonInput openJsonInput() throws IOException {
        final String cursorsHeader = getCursorsHeader();
        final Request request = requestFactory.createRequest(uri, "GET");
        if (cursorsHeader != null) {
            request.getHeaders().put("X-Nakadi-Cursors", cursorsHeader);
        }
        final Response response = request.execute();
        try {
            final Optional<String> streamId = getStreamId(response);

            if (subscription.isPresent() && streamId.isPresent()) {
                cursorManager.addStreamId(subscription.get(), streamId.get());
            }

            return new JsonInput(jsonFactory, response);
        } catch (Throwable throwable) {
            try {
                response.close();
            } catch (Throwable suppressed) {
                throwable.addSuppressed(suppressed);
            }
            throw throwable;
        }
    }

    @Nullable
    private String getCursorsHeader() throws IOException {
        if (!subscription.isPresent()) {
            final Collection<Cursor> lockedCursors = getLockedCursors();

            if (!lockedCursors.isEmpty()) {
                return cursorHeaderWriter.writeValueAsString(lockedCursors);
            }
        }
        return null;
    }

    private Collection<Cursor> getLockedCursors() throws IOException {
        final Collection<Cursor> cursors = cursorManager.getCursors(eventNames.iterator().next());
        if (lock.isPresent()) {
            final Map<String, String> offsets = cursors.stream().collect(toMap(Cursor::getPartition, Cursor::getOffset));
            final List<Partition> partitions = lock.get().getPartitions();
            return partitions.stream().map(partition -> new Cursor(partition.getPartition(), offsets.getOrDefault(partition.getPartition(), "BEGIN"))).collect(toList());
        } else {
            return cursors;
        }
    }

    private String getCurrentEventName(final Cursor cursor) {
        final String eventName = cursor.getEventType();
        return eventName != null ? eventName : eventNames.iterator().next();
    }

    private void processBatch(final Batch<T> batch) throws IOException {
        final Cursor cursor = batch.getCursor();
        final String eventName = getCurrentEventName(cursor);
        batchHandler.processBatch(new IORunnable() {
            @Override
            public void run() throws IOException {
                try {
                    listener.accept(batch.getEvents());
                    cursorManager.onSuccess(eventName, cursor);
                } catch (EventAlreadyProcessedException e) {
                    LOG.info("Events for [{}] partition [{}] at offset [{}] were already processed", eventName, cursor.getPartition(), cursor.getOffset());
                } catch (Throwable throwable) {
                    LOG.warn("Exception while processing events for [{}] on partition [{}] at offset [{}]", eventName, cursor.getPartition(), cursor.getOffset(), throwable);

                    throw throwable;
                }
            }
        });
    }

    private Cursor readCursor(JsonParser jsonParser) throws IOException {
        String partition = null;
        String offset = null;
        String eventType = null;
        String cursorToken = null;

        expectToken(jsonParser, JsonToken.START_OBJECT);

        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String field = jsonParser.getCurrentName();
            switch (field) {
                case "partition":
                    partition = jsonParser.nextTextValue();
                    break;
                case "offset":
                    offset = jsonParser.nextTextValue();
                    break;
                case "event_type":
                    eventType = jsonParser.nextTextValue();
                    break;
                case "cursor_token":
                    cursorToken = jsonParser.nextTextValue();
                    break;
                default:
                    LOG.warn("Unexpected field [{}] in cursor", field);
                    jsonParser.nextToken();
                    jsonParser.skipChildren();
                    break;
            }
        }

        if (partition == null) {
            throw new IllegalStateException("Could not read partition from cursor");
        }
        if (offset == null) {
            throw new IllegalStateException("Could not read offset from cursor for partition [" + partition + "]");
        }

        return new Cursor(partition, offset, eventType, cursorToken);
    }

    @Override
    public void run() throws IOException {
        try {
            runInternal();
        } catch (BackoffException e) {
            throw e.getCause();
        }
    }

    /*
     * @VisibleForTesting
     */
    void runInternal() throws IOException, BackoffException {
        LOG.info("Starting to listen for events for {}", eventNames);

        JsonInput jsonInput = openJsonInput();

        int errorCount = 0;

        while (true) {
            try {
                final JsonParser jsonParser = jsonInput.getJsonParser();

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedIOException("Interrupted");
                }

                readBatch(jsonParser);

                errorCount = 0;
            } catch (IOException e) {
                // Remember interrupted flag in case it accidentally gets cleared before the break
                final boolean wasInterrupted = Thread.currentThread().isInterrupted();

                metricsCollector.markErrorWhileConsuming();

                if (errorCount > 0) {
                    LOG.warn("Got [{}] [{}] while reading events for {} after [{}] retries", e.getClass().getSimpleName(), e.getMessage(), eventNames, errorCount, e);
                } else {
                    LOG.info("Got [{}] [{}] while reading events for {}", e.getClass().getSimpleName(), e.getMessage(), eventNames, e);
                }

                jsonInput.close();

                if (wasInterrupted || Thread.currentThread().isInterrupted()) {
                    LOG.warn("Thread was interrupted");
                    break;
                }

                try {
                    LOG.debug("Reconnecting after [{}] errors", errorCount);
                    jsonInput = backoffStrategy.call(errorCount, e, this::openJsonInput);
                    LOG.info("Reconnected after [{}] errors", errorCount);
                    metricsCollector.markReconnection();
                } catch (InterruptedException interruptedException) {
                    LOG.warn("Interrupted during reconnection", interruptedException);

                    Thread.currentThread().interrupt();
                    return;
                }

                errorCount++;
            } catch (Throwable e) {
                try {
                    jsonInput.close();
                } catch (Throwable suppressed) {
                    e.addSuppressed(e);
                }
                throw e;
            }
        }
    }

    /*
     * @VisibleForTesting
     */
    void readSingleBatch() throws IOException {
        try (final JsonInput jsonInput = openJsonInput()) {
            final JsonParser jsonParser = jsonInput.getJsonParser();
            readBatch(jsonParser);
        } catch (IOException e) {
            metricsCollector.markErrorWhileConsuming();
            throw e;
        }
    }

    private void readBatch(final JsonParser jsonParser) throws IOException {
        LOG.debug("Waiting for next batch of events for {}", eventNames);

        expectToken(jsonParser, JsonToken.START_OBJECT);
        metricsCollector.markMessageReceived();

        Cursor cursor = null;
        List<T> events = null;

        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            final String field = jsonParser.getCurrentName();
            switch (field) {
                case "cursor": {
                    cursor = readCursor(jsonParser);
                    break;
                }
                case "events": {
                    events = eventReader.read(jsonParser);
                    break;
                }
                case "info": {
                    LOG.debug("Skipping stream info in event batch");
                    jsonParser.nextToken();
                    jsonParser.skipChildren();
                    break;
                }
                default: {
                    LOG.warn("Unexpected field [{}] in event batch", field);
                    jsonParser.nextToken();
                    jsonParser.skipChildren();
                    break;
                }
            }
        }

        if (cursor == null) {
            throw new IOException("Could not read cursor");
        }

        final String eventName = getCurrentEventName(cursor);
        LOG.debug("Cursor for [{}] partition [{}] at offset [{}]", eventName, cursor.getPartition(), cursor.getOffset());

        if (events == null) {
            metricsCollector.markEventsReceived(0);
        } else {
            metricsCollector.markEventsReceived(events.size());

            final Batch<T> batch = new Batch<>(cursor, Collections.unmodifiableList(events));

            processBatch(batch);

            metricsCollector.markMessageSuccessfullyProcessed();
        }
    }
}
