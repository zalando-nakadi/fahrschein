package org.zalando.fahrschein;

import java.util.List;

public class EnrichedEventPersistenceException extends EventPersistenceException {

    private final List<?> inputEvents;

    public EnrichedEventPersistenceException(List<?> inputEvents, EventPersistenceException ex) {
        super(ex.getResponses());
        this.inputEvents = inputEvents;
    }

    public List<?> getInputEvents() {
        return inputEvents;
    }
}
