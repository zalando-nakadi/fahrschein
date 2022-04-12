package org.zalando.fahrschein;

@SuppressWarnings("serial")
public class EventAlreadyProcessedException extends Exception {
    public EventAlreadyProcessedException(String message) {
        super(message);
    }

    public EventAlreadyProcessedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventAlreadyProcessedException(Throwable cause) {
        super(cause);
    }
}
