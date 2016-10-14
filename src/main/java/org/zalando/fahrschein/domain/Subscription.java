package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    @Hack("Use String type for createdAt so we do not have to require JavaTimeModule to be registered in the ObjectMapper")
    public Subscription(@JsonProperty("id") String id, @JsonProperty("owning_application") String owningApplication, @JsonProperty("event_types") Set<String> eventTypes, @JsonProperty("consumer_group") String consumerGroup, @JsonProperty("created_at") String createdAt) {
        this(id, owningApplication, eventTypes, consumerGroup, OffsetDateTime.parse(createdAt));
    }

    public Subscription(String owningApplication, Set<String> eventTypes, String consumerGroup) {
        this(null, owningApplication, eventTypes, consumerGroup, (String)null);
    }

    public Subscription(String id, String owningApplication, Set<String> eventTypes, String consumerGroup, OffsetDateTime createdAt) {
        this.id = id;
        this.owningApplication = owningApplication;
        this.eventTypes = Collections.unmodifiableSet(new HashSet<>(eventTypes));
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
