package org.zalando.fahrschein;

import java.util.List;

public class EventPersistenceException extends RawEventPersistenceException {

    private final List<?> inputEvents;

    public EventPersistenceException(List<?> inputEvents, RawEventPersistenceException ex) {
        super(ex.getResponses());
        this.inputEvents = inputEvents;
    }

    public List<?> getInputEvents() {
        return inputEvents;
    }
}
