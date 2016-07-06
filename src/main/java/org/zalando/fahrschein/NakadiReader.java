package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
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

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;

public class NakadiReader<T> {

    private static final Logger LOG = LoggerFactory.getLogger(NakadiReader.class);

    private final URI uri;
    private final ClientHttpRequestFactory clientHttpRequestFactory;
    private final BackoffStrategy backoffStrategy;
    private final CursorManager cursorManager;

    private final ObjectMapper objectMapper;

    private final String eventName;
    private final Optional<Subscription> subscription;
    private final Class<T> eventClass;
    private final Listener<T> listener;

    private final JsonFactory jsonFactory;
    private final ObjectReader eventReader;

    public NakadiReader(URI uri, ClientHttpRequestFactory clientHttpRequestFactory, BackoffStrategy backoffStrategy, CursorManager cursorManager, ObjectMapper objectMapper, String eventName, Optional<Subscription> subscription, Class<T> eventClass, Listener<T> listener) {
        checkState(!subscription.isPresent() || eventName.equals(Iterables.getOnlyElement(subscription.get().getEventTypes())), "Only subscriptions to single event types are currently supported");

        this.uri = uri;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.backoffStrategy = backoffStrategy;
        this.cursorManager = cursorManager;
        this.objectMapper = objectMapper;
        this.eventName = eventName;
        this.subscription = subscription;
        this.eventClass = eventClass;
        this.listener = listener;

        this.jsonFactory = this.objectMapper.getFactory();
        this.eventReader = this.objectMapper.reader().forType(eventClass);

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

    private JsonInput openJsonInput() throws IOException {
        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(uri, HttpMethod.GET);
        if (!subscription.isPresent()) {
            final Collection<Cursor> cursors = cursorManager.getCursors(eventName);
            if (!cursors.isEmpty()) {
                final String value = objectMapper.writeValueAsString(cursors);
                request.getHeaders().put("X-Nakadi-Cursors", singletonList(value));
            }
        }
        final ClientHttpResponse response = request.execute();
        final JsonParser jsonParser = jsonFactory.createParser(response.getBody()).disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        return new JsonInput(response, jsonParser);
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

        return new Cursor(partition, offset);
    }

    private List<T> readEvents(final JsonParser jsonParser) throws IOException {
        expectToken(jsonParser, JsonToken.START_ARRAY);
        jsonParser.clearCurrentToken();

        final Iterator<T> eventIterator = eventReader.readValues(jsonParser, eventClass);

        final List<T> events = new ArrayList<>();
        while (eventIterator.hasNext()) {
            readEvent(eventIterator, events);
        }
        return events;
    }

    private void readEvent(final Iterator<T> source, final List<T> target) throws JsonMappingException {
        try {
            target.add(source.next());
        } catch (final RuntimeJsonMappingException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof JsonMappingException) {
                listener.onMappingException((JsonMappingException) cause);
            } else {
                throw e;
            }
        }
    }

    public void run() throws IOException {
        run(-1, TimeUnit.MILLISECONDS);
    }

    public void run(long timeout, TimeUnit timeoutUnit) throws IOException {

        final long lockedUntil = timeout <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeoutUnit.toMillis(timeout);

        LOG.info("Listen to events for [{}]", eventName);

        JsonInput jsonInput = openJsonInput();
        JsonParser jsonParser = jsonInput.getJsonParser();

        int errorCount = 0;

        while (System.currentTimeMillis() < lockedUntil) {
            try {

                LOG.debug("Waiting for next batch of events for [{}]", eventName);

                expectBatchStartToken(jsonParser, JsonToken.START_OBJECT);
                expectToken(jsonParser, JsonToken.FIELD_NAME);
                expectField(jsonParser, "cursor");

                final Cursor cursor = readCursor(jsonParser);

                LOG.debug("Cursor for partition [{}] at offset [{}]", cursor.getPartition(), cursor.getOffset());

                final JsonToken token = jsonParser.nextToken();
                if (token != JsonToken.END_OBJECT) {
                    expectField(jsonParser, "events");

                    final List<T> events = readEvents(jsonParser);

                    expectToken(jsonParser, JsonToken.END_OBJECT);

                    final Batch<T> batch = new Batch<>(cursor, Collections.unmodifiableList(events));

                    processBatch(batch);
                }

                errorCount = 0;
            } catch (IOException e) {
                logException(e, errorCount);

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
                } catch (BackoffException e1) {
                    LOG.warn("Could not reconnect after [{}] errors", errorCount, e1);
                    return;
                } catch (InterruptedException e1) {
                    LOG.warn("Interrupted during reconnection");

                    Thread.currentThread().interrupt();
                    return;
                }

                errorCount++;
            }
        }
    }

    private void logException(final IOException e, final int errorCount) {
        if (errorCount == 0 && e instanceof StreamClosedException) {
            if (LOG.isTraceEnabled()) {
                LOG.info("Stream was closed", e);
            } else {
                LOG.info("Stream was closed");
            }
        } else {
            LOG.warn("Got [{}] while reading events", e.getClass().getSimpleName(), e);
        }
    }

    private void expectField(JsonParser jsonParser, String expectedFieldName) throws IOException {
        final String fieldName = jsonParser.getCurrentName();
        if (fieldName == null) {
            throw new IOException("No field at current position");
        }
        checkState(expectedFieldName.equals(fieldName), "Expected [%s] field but got [%s]", expectedFieldName, fieldName);
    }

    private void expectToken(JsonParser jsonParser, JsonToken expectedToken) throws IOException {
        final JsonToken token = jsonParser.nextToken();
        if (token == null) {
            throw new IOException("Stream was closed");
        }
        checkState(token == expectedToken, "Expected [%s] but got [%s]", expectedToken, token);
    }

    private void expectBatchStartToken(final JsonParser jsonParser, final JsonToken expectedToken) throws IOException {
        final JsonToken token = jsonParser.nextToken();
        if (token == null) {
            throw new StreamClosedException("Stream was closed while waiting on next batch");
        }
        checkState(token == expectedToken, "Expected [%s] but got [%s]", expectedToken, token);
    }

}
