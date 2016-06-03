package org.zalando.fahrschein.domain;

import java.time.OffsetDateTime;

class Metadata {
    private final String eventType;
    private final String eid;
    private final OffsetDateTime occuredAt;
    private final OffsetDateTime receivedAt;
    private final String flowId;


    Metadata(String eventType, String eid, OffsetDateTime occuredAt, OffsetDateTime receivedAt, String flowId) {
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
