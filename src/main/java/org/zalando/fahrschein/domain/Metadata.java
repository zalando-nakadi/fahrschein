package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.time.OffsetDateTime;

import static org.zalando.fahrschein.domain.Metadata.FieldNames.*;

@Immutable
public final class Metadata {
    class FieldNames {
        static final String EVENT_TYPE = "event_type";
        static final String EID = "eid";
        static final String OCCURED_AT = "occured_at";
        static final String RECEIVED_AT = "received_at";
        static final String FLOW_ID = "flow_id";
    }

    @JsonProperty(EVENT_TYPE)
    private final String eventType;

    private final String eid;

    @JsonProperty(OCCURED_AT)
    private final OffsetDateTime occuredAt;

    @JsonProperty(RECEIVED_AT)
    private final OffsetDateTime receivedAt;

    @JsonProperty(FLOW_ID)
    private final String flowId;

    @JsonCreator
    @Deprecated
    public Metadata(@JsonProperty(EVENT_TYPE) String eventType, @JsonProperty(EID) String eid, @JsonProperty(OCCURED_AT) String occuredAt, @JsonProperty(RECEIVED_AT) String receivedAt, @JsonProperty(FLOW_ID) String flowId) {
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
