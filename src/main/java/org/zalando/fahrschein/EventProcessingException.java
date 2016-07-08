package org.zalando.fahrschein;

import java.io.IOException;

@SuppressWarnings("serial")
public class EventProcessingException extends IOException {

    public EventProcessingException(String message) {
        super(message);
    }

    public EventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventProcessingException(Throwable cause) {
        super(cause);
    }
}
