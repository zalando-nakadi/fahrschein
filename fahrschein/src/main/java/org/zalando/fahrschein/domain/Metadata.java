package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class Metadata {
    private final String eventType;
    private final String eid;
    private final String occurredAt;
    private final String receivedAt;
    private final String flowId;

    @JsonCreator
    @Deprecated
    private Metadata(@JsonProperty("event_type") String eventType, @JsonProperty("eid") String eid, @JsonProperty("occurred_at") String occurredAt, @JsonProperty("received_at") String receivedAt, @JsonProperty("flow_id") String flowId) {
        this.eventType = eventType;
        this.eid = eid;
        this.occurredAt = occurredAt;
        this.receivedAt = receivedAt;
        this.flowId = flowId;
    }

    public Metadata(String eid, String occurredAt) {
        this(null, eid, occurredAt, null, null);
    }

    public String getEventType() {
        return eventType;
    }

    public String getEid() {
        return eid;
    }

    public String getOccurredAt() {
        return occurredAt;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public String getFlowId() {
        return flowId;
    }
}
