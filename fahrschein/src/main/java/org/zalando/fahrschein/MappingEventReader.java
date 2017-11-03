package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.zalando.fahrschein.JsonParserHelper.expectToken;

public class MappingEventReader<T> implements EventReader<T> {
    private final Class<T> eventClass;
    private final ObjectReader eventReader;

    public MappingEventReader(Class<T> eventClass, ObjectMapper objectMapper) {
        this.eventClass = eventClass;
        this.eventReader = objectMapper.reader().forType(eventClass);
    }

    @Override
    public List<T> read(JsonParser jsonParser) throws IOException {
        expectToken(jsonParser, JsonToken.START_ARRAY);
        jsonParser.clearCurrentToken();

        final MappingIterator<T> eventIterator = eventReader.readValues(jsonParser);

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
                    onMappingException((JsonMappingException) cause);
                } else if (cause instanceof IOException) {
                    throw (IOException)cause;
                } else {
                    throw e;
                }
            }
        }
        return events;

    }

    protected void onMappingException(JsonMappingException exception) throws IOException {
        throw exception;
    }

}
