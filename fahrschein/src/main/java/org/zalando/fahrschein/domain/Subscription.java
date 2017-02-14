package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

@Immutable
public class Subscription {
    private final String id;
    private final String owningApplication;
    private final Set<String> eventTypes;
    private final String consumerGroup;
    private final Date createdAt;

    @JsonCreator
    public Subscription(@JsonProperty("id") String id, @JsonProperty("owning_application") String owningApplication, @JsonProperty("event_types") Set<String> eventTypes, @JsonProperty("consumer_group") String consumerGroup, @JsonProperty("created_at") Date createdAt) {
        this.id = id;
        this.owningApplication = owningApplication;
        this.eventTypes = unmodifiableSet(eventTypes == null ? Collections.<String>emptySet() : new HashSet<>(eventTypes));
        this.consumerGroup = consumerGroup;
        this.createdAt = createdAt;
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

    public Date getCreatedAt() {
        return createdAt;
    }
}
