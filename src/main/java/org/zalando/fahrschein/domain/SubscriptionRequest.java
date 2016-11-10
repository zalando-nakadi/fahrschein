package org.zalando.fahrschein.domain;

import javax.annotation.concurrent.Immutable;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@Immutable
public class SubscriptionRequest {
    private final String owningApplication;
    private final Set<String> eventTypes;
    private final String consumerGroup;


    public SubscriptionRequest(String owningApplication, Set<String> eventTypes, String consumerGroup) {
        this.owningApplication = owningApplication;
        this.eventTypes = unmodifiableSet(eventTypes == null ? emptySet() : new HashSet<>(eventTypes));
        this.consumerGroup = consumerGroup;
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
}
