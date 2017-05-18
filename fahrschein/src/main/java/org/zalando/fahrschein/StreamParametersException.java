package org.zalando.fahrschein;

import java.io.IOException;

@SuppressWarnings("serial")
public class StreamParametersException extends IOException {
    public StreamParametersException(String message) {
        super(message);
    }

    public StreamParametersException(String message, Throwable cause) {
        super(message, cause);
    }

    public StreamParametersException(Throwable cause) {
        super(cause);
    }
}
