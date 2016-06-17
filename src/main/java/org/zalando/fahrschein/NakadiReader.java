package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.gag.annotation.remark.Hack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.domain.Batch;
import org.zalando.fahrschein.domain.Cursor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.joining;

public class NakadiReader<T> implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NakadiReader.class);

    private final InputStreamSupplier inputStreamSupplier;
    private final ConnectionParameters connectionParameters;
    private final AccessTokenProvider accessTokenProvider;
    private final CursorManager cursorManager;

    private final ObjectMapper objectMapper;

    private final String eventName;
    private final Class<T> eventClass;
    private final Listener<T> listener;

    public NakadiReader(InputStreamSupplier inputStreamSupplier, ConnectionParameters connectionParameters, AccessTokenProvider accessTokenProvider, CursorManager cursorManager, ObjectMapper objectMapper, String eventName, Class<T> eventClass, Listener<T> listener) {
        this.inputStreamSupplier = inputStreamSupplier;
        this.connectionParameters = connectionParameters;
        this.accessTokenProvider = accessTokenProvider;
        this.cursorManager = cursorManager;
        this.objectMapper = objectMapper;
        this.eventName = eventName;
        this.eventClass = eventClass;
        this.listener = listener;
    }

    private String formatCursor(Cursor cursor) {
        return String.format("{\"partition\":\"%s\",\"offset\":\"%s\"}", cursor.getPartition(), cursor.getOffset());
    }

    @Hack("Should use proper json library")
    private String formatCursors(Collection<Cursor> cursors) {

        return cursors
                .stream()
                .map(this::formatCursor)
                .collect(joining(",", "[", "]"));
    }

    private Map<String, String> cursorHeader() {
        final Map<String, String> headers = new HashMap<>();


        final Collection<Cursor> cursors = cursorManager.getCursors(eventName);
        if (!cursors.isEmpty()) {
            headers.put("X-Nakadi-Cursors", formatCursors(cursors));
        }

        return headers;
    }

    private InputStream open(int errorCount) throws IOException, InterruptedException {
        final ConnectionParameters connectionParameters =
                this.connectionParameters.withErrorCount(errorCount)
                                         .withHeaders(cursorHeader())
                                         .withAuthorization("Bearer ".concat(accessTokenProvider.getAccessToken()));
        return inputStreamSupplier.open(connectionParameters);
    }

    private void processBatch(Batch<T> batch) throws EventProcessingException {
        final Cursor cursor = batch.getCursor();
        try {
            listener.onEvent(batch.getEvents());
            cursorManager.onSuccess(eventName, cursor);
        } catch (EventProcessingException e) {
            cursorManager.onError(eventName, cursor, e);
            throw e;
        }
    }

    private Cursor readCursor(JsonParser jsonParser) throws IOException {
        String partition = null;
        String offset = null;

        final JsonToken token = jsonParser.nextToken();
        checkState(token == JsonToken.START_OBJECT);

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
        final JsonToken token = jsonParser.nextToken();
        checkState(token == JsonToken.START_ARRAY);

        final List<T> events = new ArrayList<>();

        while (jsonParser.nextToken() == JsonToken.START_OBJECT) {
            final T event = objectReader.readValue(jsonParser, eventClass);
            events.add(event);
        }

        return events;
    }

    public void run() {

        final JsonFactory jsonFactory = objectMapper.copy().getFactory().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        final ObjectReader objectReader = objectMapper.reader().forType(eventClass);

        InputStream inputStream;

        try {
            inputStream = open(0);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted during initial connection");
            Thread.currentThread().interrupt();
            return;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        int errorCount = 0;

        while (true) {
            try {
                final JsonParser jsonParser = jsonFactory.createParser(inputStream);

                JsonToken token;
                String field;

                token = jsonParser.nextToken();
                checkState(token == JsonToken.START_OBJECT);
                field = jsonParser.nextFieldName();
                checkState("cursor".equals(field));

                final Cursor cursor = readCursor(jsonParser);

                LOG.debug("Cursor for partition [{}] at offset [{}]", cursor.getPartition(), cursor.getOffset());

                token = jsonParser.nextToken();
                if (token != JsonToken.END_OBJECT) {
                    field = jsonParser.getCurrentName();
                    checkState("events".equals(field));

                    final List<T> events = readEvents(objectReader, jsonParser);

                    token = jsonParser.nextToken();
                    checkState(token == JsonToken.END_OBJECT);

                    final Batch<T> batch = new Batch<>(cursor, Collections.unmodifiableList(events));

                    processBatch(batch);

                }
                errorCount = 0;
            } catch (IOException e) {

                LOG.warn("Got [{}] while reading events", e.getClass().getSimpleName(), e);
                try {
                    LOG.debug("Trying to close input stream");
                    inputStream.close();
                } catch (IOException e1) {
                    LOG.warn("Could not close input stream on IOException");
                }

                try {
                    LOG.info("Reconnecting after [{}] errors", errorCount);
                    inputStream = open(errorCount);
                } catch (InterruptedException e1) {
                    LOG.warn("Interrupted during reconnection");

                    Thread.currentThread().interrupt();
                    return;
                } catch (IOException e1) {
                    throw new UncheckedIOException(e);
                }

                errorCount++;
            }
        }
    }

}
