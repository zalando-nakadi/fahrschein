package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.fahrschein.domain.Batch;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;

public class NakadiReader<T> {

    private static final Logger LOG = LoggerFactory.getLogger(NakadiReader.class);

    private final URI uri;
    private final ClientHttpRequestFactory clientHttpRequestFactory;
    private final ExponentialBackoffStrategy exponentialBackoffStrategy;
    private final CursorManager cursorManager;

    private final ObjectMapper objectMapper;

    private final String eventName;
    private final Optional<Subscription> subscription;
    private final Class<T> eventClass;
    private final Listener<T> listener;

    public NakadiReader(URI uri, ClientHttpRequestFactory clientHttpRequestFactory, ExponentialBackoffStrategy exponentialBackoffStrategy, CursorManager cursorManager, ObjectMapper objectMapper, String eventName, Optional<Subscription> subscription, Class<T> eventClass, Listener<T> listener) {
        this.uri = uri;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.exponentialBackoffStrategy = exponentialBackoffStrategy;
        this.cursorManager = cursorManager;
        this.objectMapper = objectMapper;
        this.eventName = eventName;
        this.subscription = subscription;
        this.eventClass = eventClass;
        this.listener = listener;

        checkState(!subscription.isPresent() || eventName.equals(Iterables.getOnlyElement(subscription.get().getEventTypes())));
    }

    private ClientHttpResponse openStream(int errorCount) throws InterruptedException, IOException {
        try {
            return exponentialBackoffStrategy.call(errorCount, this::openStream);
        } catch (ExponentialBackoffException e) {
            throw e.getCause();
        }
    }

    private ClientHttpResponse openStream() throws IOException {
        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(uri, HttpMethod.GET);
        if (!subscription.isPresent()) {
            final Collection<Cursor> cursors = cursorManager.getCursors(eventName);
            if (!cursors.isEmpty()) {
                final String value = objectMapper.writeValueAsString(cursors);
                request.getHeaders().put("X-Nakadi-Cursors", singletonList(value));
            }
        }
        return request.execute();
    }

    private void processBatch(Batch<T> batch) throws IOException {
        final Cursor cursor = batch.getCursor();
        try {
            listener.onEvent(batch.getEvents());
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

    private List<T> readEvents(ObjectReader objectReader, JsonParser jsonParser) throws IOException {
        expectToken(jsonParser, JsonToken.START_ARRAY);

        final List<T> events = new ArrayList<>();

        while (jsonParser.nextToken() == JsonToken.START_OBJECT) {
            final T event = objectReader.readValue(jsonParser, eventClass);
            events.add(event);
        }

        return events;
    }

    public void run() throws IOException {
        run(-1, TimeUnit.MILLISECONDS);
    }

    public void run(long timeout, TimeUnit timeoutUnit) throws IOException {

        final long lockedUntil = timeout <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeoutUnit.toMillis(timeout);

        final JsonFactory jsonFactory = objectMapper.copy().getFactory().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        final ObjectReader objectReader = objectMapper.reader().forType(eventClass);

        ClientHttpResponse response = openStream();
        JsonParser jsonParser = jsonFactory.createParser(response.getBody());

        int errorCount = 0;

        while (System.currentTimeMillis() < lockedUntil) {
            try {

                expectToken(jsonParser, JsonToken.START_OBJECT);
                expectField(jsonParser, "cursor");

                final Cursor cursor = readCursor(jsonParser);

                LOG.debug("Cursor for partition [{}] at offset [{}]", cursor.getPartition(), cursor.getOffset());

                final JsonToken token = jsonParser.nextToken();
                if (token != JsonToken.END_OBJECT) {
                    expectField(jsonParser, "events");

                    final List<T> events = readEvents(objectReader, jsonParser);

                    expectToken(jsonParser, JsonToken.END_OBJECT);

                    final Batch<T> batch = new Batch<>(cursor, Collections.unmodifiableList(events));

                    processBatch(batch);
                }
                errorCount = 0;
            } catch (IOException e) {

                LOG.warn("Got [{}] while reading events", e.getClass().getSimpleName(), e);
                try {
                    LOG.debug("Trying to close json parser");
                    jsonParser.close();
                } catch (IOException e1) {
                    LOG.warn("Could not close json parser on IOException");
                } finally {
                    LOG.debug("Trying to close response");
                    response.close();
                }

                if (Thread.currentThread().isInterrupted()) {
                    LOG.warn("Thread was interruped");
                    break;
                }

                try {
                    LOG.info("Reconnecting after [{}] errors", errorCount);
                    response = openStream(errorCount);
                    jsonParser = jsonFactory.createParser(response.getBody());
                } catch (InterruptedException e1) {
                    LOG.warn("Interrupted during reconnection");

                    Thread.currentThread().interrupt();
                    return;
                }

                errorCount++;
            }
        }
    }

    private void expectField(JsonParser jsonParser, String expectedFieldName) throws IOException {
        final String fieldName = jsonParser.nextFieldName();
        if (fieldName == null) {
            throw new IOException("Stream was closed or no field at current position");
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

}
