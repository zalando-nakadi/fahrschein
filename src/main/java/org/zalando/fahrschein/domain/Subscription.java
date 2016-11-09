package org.zalando.fahrschein.domain;

import com.google.gag.annotation.remark.Hack;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Immutable
public class Subscription {
    @Nullable
    private final String id;
    private final String owningApplication;
    private final Set<String> eventTypes;
    private final String consumerGroup;
    @Nullable
    private final OffsetDateTime createdAt;

    @Hack("Necessary to enable Jackson Wrapper working with multiple constructors but without @JsonCreator annotation")
    private Subscription() {
        this(null, null, null, null, null);
    }

    public Subscription(String id, String owningApplication, Set<String> eventTypes, String consumerGroup, OffsetDateTime createdAt) {
        this.id = id;
        this.owningApplication = owningApplication;
        this.eventTypes = determineEventTypesToBeSet(eventTypes);
        this.consumerGroup = consumerGroup;
        this.createdAt = createdAt;
    }

    @Hack("Necessary to prevent NullPointerException on deserialization")
    private Set<String> determineEventTypesToBeSet(Set<String> eventTypes) {
        Set<String> eventTypesToBeSet;
        if (eventTypes != null)
            eventTypesToBeSet = Collections.unmodifiableSet(new HashSet<>(eventTypes));
        else {
            eventTypesToBeSet = new HashSet<>();
        }
        return eventTypesToBeSet;
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
