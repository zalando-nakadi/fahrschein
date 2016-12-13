package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.time.OffsetDateTime;

@Immutable
public final class Metadata {
    private final String eventType;
    private final String eid;
    private final OffsetDateTime occuredAt;
    private final OffsetDateTime receivedAt;
    private final String flowId;

    @JsonCreator
    @Deprecated
    private Metadata(@JsonProperty("event_type") String eventType, @JsonProperty("eid") String eid, @JsonProperty("occured_at") String occuredAt, @JsonProperty("received_at") String receivedAt, @JsonProperty("flow_id") String flowId) {
        this(eventType, eid, occuredAt == null ? null : OffsetDateTime.parse(occuredAt), receivedAt == null ? null : OffsetDateTime.parse(receivedAt), flowId);
    }

    public Metadata(String eventType, String eid, OffsetDateTime occuredAt, OffsetDateTime receivedAt, String flowId) {
        this.eventType = eventType;
        this.eid = eid;
        this.occuredAt = occuredAt;
        this.receivedAt = receivedAt;
        this.flowId = flowId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEid() {
        return eid;
    }

    public OffsetDateTime getOccuredAt() {
        return occuredAt;
    }

    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }

    public String getFlowId() {
        return flowId;
    }
}
