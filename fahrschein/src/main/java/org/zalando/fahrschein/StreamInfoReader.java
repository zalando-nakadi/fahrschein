package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.Optional;

public interface StreamInfoReader {

    Optional<String> readDebug(JsonParser parser) throws IOException;

    static StreamInfoReader getDefault() {
        return new DefaultImpl();
    }

    class DefaultImpl implements StreamInfoReader {

        @Override
        public Optional<String> readDebug(JsonParser parser) throws IOException {
            Optional<String> debug = Optional.empty();

            JsonParserHelper.expectToken(parser, JsonToken.START_OBJECT);
            while(parser.nextToken() != JsonToken.END_OBJECT) {
                final String currentFieldName = parser.getCurrentName();
                switch (currentFieldName) {
                    case "debug":
                        debug = Optional.ofNullable(parser.nextTextValue());
                        break;
                    default:
                        parser.nextToken();
                        parser.skipChildren();
                        break;
                }
            }
            return debug;
        }



    }

}
