package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.concurrent.Immutable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@Immutable
public class Subscription {
    private final String id;
    private final String owningApplication;
    private final Set<String> eventTypes;
    private final String consumerGroup;
    private final OffsetDateTime createdAt;
    private final Authorization authorization;

    public Subscription(String id, String owningApplication, Set<String> eventTypes, String consumerGroup, OffsetDateTime createdAt, Authorization authorization) {
        this.id = id;
        this.owningApplication = owningApplication;
        this.eventTypes = unmodifiableSet(eventTypes == null ? emptySet() : new HashSet<>(eventTypes));
        this.consumerGroup = consumerGroup;
        this.createdAt = createdAt;
        this.authorization = authorization;
    }

    //Ignoring this constructor from introspection-based serialization and deserialization functionality,so that It doesn't
    //interfere with default-all-args constructor.
    @JsonIgnore
    public Subscription(String id, String owningApplication, Set<String> eventTypes) {
        this(id, owningApplication, eventTypes, null, null, null);
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

    public Authorization getAuthorization() {
        return authorization;
    }
}
