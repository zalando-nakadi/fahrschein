package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

@Immutable
public class SubscriptionRequest {

    public enum Position {
        BEGIN("begin"), END("end"), CURSORS("cursors");

        private final String value;

        Position(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    private final String owningApplication;
    private final Set<String> eventTypes;
    private final String consumerGroup;
    private final Position readFrom;
    private final List<Cursor> initialCursors;

    public SubscriptionRequest(String owningApplication, Set<String> eventTypes, String consumerGroup, Position readFrom, List<Cursor> initialCursors) {
        this.owningApplication = owningApplication;
        this.eventTypes = unmodifiableSet(eventTypes == null ? Collections.<String>emptySet() : new LinkedHashSet<>(eventTypes));
        this.consumerGroup = consumerGroup;
        this.readFrom = readFrom;
        this.initialCursors = unmodifiableList(initialCursors == null ? Collections.<Cursor>emptyList() : new ArrayList<>(initialCursors));
    }

    public SubscriptionRequest(String owningApplication, Set<String> eventTypes, String consumerGroup) {
        this(owningApplication, eventTypes, consumerGroup, Position.END, Collections.<Cursor>emptyList());
    }

    public String getOwningApplication() {
        return owningApplication;
    }

    public Set<String> getEventTypes() {
        return eventTypes;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public Position getReadFrom() {
        return readFrom;
    }

    public List<Cursor> getInitialCursors() {
        return initialCursors;
    }
}
