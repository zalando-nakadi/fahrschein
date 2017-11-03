package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import javax.annotation.Nullable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;

class JsonParserHelper {
    private JsonParserHelper() {
    }

    static void expectToken(JsonParser jsonParser, JsonToken expectedToken) throws IOException {
        final JsonToken token = jsonParser.nextToken();
        expectToken(token, expectedToken);
    }

    static void expectToken(@Nullable JsonToken currentToken, JsonToken expectedToken) throws IOException {
        if (currentToken == null) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedIOException("Thread was interrupted");
            } else {
                throw new EOFException("Stream was closed");
            }
        }
        if (currentToken != expectedToken) {
            throw new IOException(String.format("Expected [%s] but got [%s]", expectedToken, currentToken));
        }
    }
}
