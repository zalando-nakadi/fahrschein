package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.*;

import static java.util.Collections.*;

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
    private final Authorization authorization;

    public SubscriptionRequest(String owningApplication, @Nullable Set<String> eventTypes, String consumerGroup, Position readFrom, @Nullable List<Cursor> initialCursors, @Nullable Authorization authorization) {
        this.owningApplication = owningApplication;
        this.eventTypes = unmodifiableSet(eventTypes == null ? emptySet() : new HashSet<>(eventTypes));
        this.consumerGroup = consumerGroup;
        this.readFrom = readFrom;
        this.initialCursors = unmodifiableList((initialCursors == null) ? emptyList() : new ArrayList<>(initialCursors));
        this.authorization = authorization;
    }

    public SubscriptionRequest(String owningApplication, Set<String> eventTypes, String consumerGroup) {
        this(owningApplication, eventTypes, consumerGroup, Position.END, emptyList(), null);
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

    public Authorization getAuthorization() {
        return authorization;
    }
}
