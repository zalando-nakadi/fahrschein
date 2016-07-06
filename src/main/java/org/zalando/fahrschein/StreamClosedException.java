package org.zalando.fahrschein;

import java.io.IOException;

public class StreamClosedException extends IOException {

    public StreamClosedException(final String message) {
        super(message);
    }
}
