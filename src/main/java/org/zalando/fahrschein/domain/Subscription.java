package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gag.annotation.remark.Hack;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.zalando.fahrschein.domain.Subscription.FieldNames.*;

@Immutable
public class Subscription {

    static class FieldNames {
        static final String OWNING_APPLICATION = "owning_application";
        static final String EVENT_TYPES = "event_types";
        static final String CONSUMER_GROUP = "consumer_group";
        static final String CREATED_AT = "created_at";
        static final String ID = "id";
    }
    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String id;
    @JsonProperty(OWNING_APPLICATION)
    private final String owningApplication;
    @JsonProperty(EVENT_TYPES)
    private final Set<String> eventTypes;
    @JsonProperty(CONSUMER_GROUP)
    private final String consumerGroup;
    @Nullable
    @JsonProperty(CREATED_AT)
    private final OffsetDateTime createdAt;

    @JsonCreator
    @Hack("Use String type for createdAt so we do not have to require JavaTimeModule to be registered in the ObjectMapper")
    public Subscription(@JsonProperty(ID) String id, @JsonProperty(OWNING_APPLICATION) String owningApplication, @JsonProperty(EVENT_TYPES) Set<String> eventTypes, @JsonProperty(CONSUMER_GROUP) String consumerGroup, @JsonProperty(CREATED_AT) String createdAt) {
        this(id, owningApplication, eventTypes, consumerGroup, createdAt == null ? null : OffsetDateTime.parse(createdAt));
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
