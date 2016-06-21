package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Subscription {
    @Nullable
    private final String id;
    private final String owningApplication;
    private final Set<String> eventTypes;
    private final String consumerGroup;
    @Nullable
    private final OffsetDateTime createdAt;

    @JsonCreator
    public Subscription(String id, String owningApplication, Set<String> eventTypes, String consumerGroup, OffsetDateTime createdAt) {
        this.id = id;
        this.owningApplication = owningApplication;
        this.eventTypes = Collections.unmodifiableSet(new HashSet<>(eventTypes));
        this.consumerGroup = consumerGroup;
        this.createdAt = createdAt;
    }

    public Subscription(String owningApplication, Set<String> eventTypes, String consumerGroup) {
        this(null, owningApplication, eventTypes, consumerGroup, null);
    }

    public String getId() {
        return id;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
