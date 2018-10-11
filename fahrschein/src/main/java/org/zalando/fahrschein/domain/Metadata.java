package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Immutable
public final class Metadata {
    private final String eventType;
    private final String eid;
    private final OffsetDateTime occurredAt;
    private final OffsetDateTime receivedAt;
    private final String flowId;
    private final Map<String, Object> other = new HashMap<String, Object>();

    @JsonCreator
    @Deprecated
    private Metadata(@JsonProperty("event_type") String eventType, @JsonProperty("eid") String eid, @JsonProperty("occurred_at") String occurredAt, @JsonProperty("received_at") String receivedAt, @JsonProperty("flow_id") String flowId) {
        this(eventType, eid, occurredAt == null ? null : OffsetDateTime.parse(occurredAt), receivedAt == null ? null : OffsetDateTime.parse(receivedAt), flowId);
    }

    public Metadata(String eventType, String eid, OffsetDateTime occurredAt, OffsetDateTime receivedAt, String flowId) {
        this.eventType = eventType;
        this.eid = eid;
        this.occurredAt = occurredAt;
        this.receivedAt = receivedAt;
        this.flowId = flowId;
    }

    public Metadata(String eid, OffsetDateTime occurredAt) {
        this(null, eid, occurredAt, null, null);
    }

    public String getEventType() {
        return eventType;
    }

    public String getEid() {
        return eid;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }

    public String getFlowId() {
        return flowId;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        other.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, Object> any() {
        return Collections.unmodifiableMap(other);
    }

    public Object get(String name) {
        return other.get(name);
    }
}
