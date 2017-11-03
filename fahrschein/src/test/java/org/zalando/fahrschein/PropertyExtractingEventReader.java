package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.zalando.fahrschein.JsonParserHelper.expectToken;

public abstract class PropertyExtractingEventReader<T> implements EventReader<T> {

    private final String propertyName;

    public PropertyExtractingEventReader(String propertyName) {
        this.propertyName = propertyName;
    }

    protected abstract T getValue(JsonParser jsonParser) throws IOException;

    @Override
    public List<T> read(JsonParser jsonParser) throws IOException {
        expectToken(jsonParser.nextToken(), JsonToken.START_ARRAY);

        final List<T> result = new ArrayList<>();

        for (JsonToken token = jsonParser.nextToken(); token != JsonToken.END_ARRAY; token = jsonParser.nextToken()) {
            expectToken(token, JsonToken.START_OBJECT);

            for (token = jsonParser.nextToken(); token != JsonToken.END_OBJECT; token = jsonParser.nextToken()) {
                expectToken(token, JsonToken.FIELD_NAME);

                final String topLevelProperty = jsonParser.getCurrentName();
                jsonParser.nextToken();

                if (propertyName.equals(topLevelProperty)) {
                    final T value = getValue(jsonParser);
                    result.add(value);
                } else {
                    jsonParser.skipChildren();
                }
            }
        }

        return result;
    }
}
