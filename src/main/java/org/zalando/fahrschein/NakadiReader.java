package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.zalando.fahrschein.domain.Batch;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.metrics.MetricsCollector;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class NakadiReader<T> implements IORunnable {

    private static final Logger LOG = LoggerFactory.getLogger(NakadiReader.class);
    private static final TypeReference<List<Cursor>> LIST_OF_CURSORS = new TypeReference<List<Cursor>>() {
    };

    private final URI uri;
    private final ClientHttpRequestFactory clientHttpRequestFactory;
    private final BackoffStrategy backoffStrategy;
    private final CursorManager cursorManager;

    private final String eventName;
    private final Optional<Subscription> subscription;
    private final Class<T> eventClass;
    private final Listener<T> listener;

    private final JsonFactory jsonFactory;
    private final ObjectReader eventReader;
    private final ObjectWriter cursorHeaderWriter;

    private final MetricsCollector metricsCollector;

    NakadiReader(URI uri, ClientHttpRequestFactory clientHttpRequestFactory, BackoffStrategy backoffStrategy, CursorManager cursorManager, ObjectMapper objectMapper, String eventName, Optional<Subscription> subscription, Class<T> eventClass, Listener<T> listener, final MetricsCollector metricsCollector) {
        checkState(!subscription.isPresent() || eventName.equals(Iterables.getOnlyElement(subscription.get().getEventTypes())), "Only subscriptions to single event types are currently supported");

        this.uri = uri;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.backoffStrategy = backoffStrategy;
        this.cursorManager = cursorManager;
        this.eventName = eventName;
        this.subscription = subscription;
        this.eventClass = eventClass;
        this.listener = listener;
        this.metricsCollector = metricsCollector;

        this.jsonFactory = objectMapper.getFactory();
        this.eventReader = objectMapper.reader().forType(eventClass);
        this.cursorHeaderWriter = objectMapper.writerFor(LIST_OF_CURSORS).without(SerializationFeature.INDENT_OUTPUT);

        if (clientHttpRequestFactory instanceof HttpComponentsClientHttpRequestFactory) {
            LOG.warn("Using [{}] might block during reconnection, please consider using another implementation of ClientHttpRequestFactory", clientHttpRequestFactory.getClass().getName());
        }
    }

    static class JsonInput implements Closeable {
        private final ClientHttpResponse response;
        private final JsonParser jsonParser;

        JsonInput(ClientHttpResponse response, JsonParser jsonParser) {
            this.response = response;
            this.jsonParser = jsonParser;
        }

        ClientHttpResponse getResponse() {
            return response;
        }

        JsonParser getJsonParser() {
            return jsonParser;
        }

        @Override
        public void close() {
            try {
                LOG.trace("Trying to close json parser");
                jsonParser.close();
                LOG.trace("Closed json parser");
            } catch (IOException e) {
                LOG.warn("Could not close json parser", e);
            } finally {
                LOG.trace("Trying to close response");
                response.close();
                LOG.trace("Closed response");
            }
        }
    }

    private static Optional<String> getStreamId(ClientHttpResponse response) {
        return Optional.ofNullable(response.getHeaders())
                .flatMap(h -> h.getOrDefault("X-Nakadi-StreamId", emptyList()).stream().findFirst());
    }

    private JsonInput openJsonInput() throws IOException {
        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(uri, HttpMethod.GET);
        if (!subscription.isPresent()) {
            final Collection<Cursor> cursors = cursorManager.getCursors(eventName);
            if (!cursors.isEmpty()) {
                final String value = cursorHeaderWriter.writeValueAsString(cursors);
                request.getHeaders().put("X-Nakadi-Cursors", singletonList(value));
            }
        }
        final ClientHttpResponse response = request.execute();
        try {
            final Optional<String> streamId = getStreamId(response);
            final JsonParser jsonParser = jsonFactory.createParser(response.getBody()).disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);

            if (subscription.isPresent() && streamId.isPresent()) {
                cursorManager.addStreamId(subscription.get(), streamId.get());
            }

            return new JsonInput(response, jsonParser);
        } catch (Throwable throwable) {
            try {
                response.close();
            } catch (Throwable suppressed) {
                throwable.addSuppressed(suppressed);
            }
            throw throwable;
        }
    }

    private void processBatch(Batch<T> batch) throws IOException {
        final Cursor cursor = batch.getCursor();
        try {
            listener.accept(batch.getEvents());
            cursorManager.onSuccess(eventName, cursor);
        } catch (EventAlreadyProcessedException e) {
            LOG.info("Events for [{}] partition [{}] at offset [{}] were already processed", eventName, cursor.getPartition(), cursor.getOffset());
        } catch (Throwable throwable) {
            cursorManager.onError(eventName, cursor, throwable);
            throw throwable;
        }
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

    private List<T> readEvents(final JsonParser jsonParser) throws IOException {
        expectToken(jsonParser, JsonToken.START_ARRAY);
        jsonParser.clearCurrentToken();

        final Iterator<T> eventIterator = eventReader.readValues(jsonParser, eventClass);

        final List<T> events = new ArrayList<>();
        while (true) {
            try {
                // MappingIterator#hasNext can theoretically also throw RuntimeExceptions, that's why we use this strange loop structure
                if (eventIterator.hasNext()) {
                    events.add(eventClass.cast(eventIterator.next()));
                } else {
                    break;
                }
            } catch (RuntimeException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof JsonMappingException) {
                    listener.onMappingException((JsonMappingException) cause);
                } else if (cause instanceof IOException) {
                    throw (IOException)cause;
                } else {
                    throw e;
                }
            }
        }
        return events;
    }

    @Override
    public void run() throws IOException {
        try {
            runInternal();
        } catch (BackoffException e) {
            throw e.getCause();
        }
    }

    @VisibleForTesting
    void runInternal() throws IOException, BackoffException {
        LOG.info("Starting to listen for events for [{}]", eventName);

        JsonInput jsonInput = openJsonInput();
        JsonParser jsonParser = jsonInput.getJsonParser();

        int errorCount = 0;

        while (true) {
            try {
                LOG.debug("Waiting for next batch of events for [{}]", eventName);

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
                            events = readEvents(jsonParser);
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

                LOG.debug("Cursor for [{}] partition [{}] at offset [{}]", eventName, cursor.getPartition(), cursor.getOffset());

                if (events == null) {
                    metricsCollector.markEventsReceived(0);
                } else {
                    metricsCollector.markEventsReceived(events.size());

                    final Batch<T> batch = new Batch<>(cursor, Collections.unmodifiableList(events));

                    processBatch(batch);

                    metricsCollector.markMessageSuccessfullyProcessed();
                }

                errorCount = 0;
            } catch (IOException e) {

                metricsCollector.markErrorWhileConsuming();

                if (errorCount > 0) {
                    LOG.warn("Got [{}] [{}] while reading events for [{}] after [{}] retries", e.getClass().getSimpleName(), e.getMessage(), eventName, errorCount, e);
                } else {
                    LOG.info("Got [{}] [{}] while reading events for [{}]", e.getClass().getSimpleName(), e.getMessage(), eventName, e);
                }

                jsonInput.close();

                if (Thread.currentThread().isInterrupted()) {
                    LOG.warn("Thread was interrupted");
                    break;
                }

                try {
                    LOG.debug("Reconnecting after [{}] errors", errorCount);
                    jsonInput = backoffStrategy.call(errorCount, e, this::openJsonInput);
                    jsonParser = jsonInput.getJsonParser();
                    LOG.info("Reconnected after [{}] errors", errorCount);
                    metricsCollector.markReconnection();
                } catch (InterruptedException interruptedException) {
                    LOG.warn("Interrupted during reconnection", interruptedException);

                    Thread.currentThread().interrupt();
                    return;
                }

                errorCount++;
            }
        }
    }

    private void expectToken(JsonParser jsonParser, JsonToken expectedToken) throws IOException {
        final JsonToken token = jsonParser.nextToken();
        if (token == null) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedIOException("Thread was interrupted");
            } else {
                throw new EOFException("Stream was closed");
            }
        }
        if (token != expectedToken) {
            throw new IOException(String.format("Expected [%s] but got [%s]", expectedToken, token));
        }
    }

}
