package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.concurrent.Immutable;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@Immutable
public class SubscriptionRequest {

    public enum Position {
        BEGIN("begin"), END("end");

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

    public SubscriptionRequest(String owningApplication, Set<String> eventTypes, String consumerGroup, Position readFrom) {
        this.owningApplication = owningApplication;
        this.eventTypes = unmodifiableSet(eventTypes == null ? emptySet() : new HashSet<>(eventTypes));
        this.consumerGroup = consumerGroup;
        this.readFrom = readFrom;
    }

    public SubscriptionRequest(String owningApplication, Set<String> eventTypes, String consumerGroup) {
        this(owningApplication, eventTypes, consumerGroup, Position.END);
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
}
