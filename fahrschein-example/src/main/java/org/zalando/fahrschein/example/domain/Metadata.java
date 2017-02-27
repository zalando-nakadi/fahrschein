package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by akukuljac on 27/02/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaData {

    private Date occuredAt;
    private String eid;
    private String eventType;
    private Date receivedAt;
    private String flowId;

    @JsonCreator
    public MetaData(@JsonProperty("occurred_at") Date occuredAt, @JsonProperty("eid") String eid,
                    @JsonProperty("event_type") String eventType, @JsonProperty("received_at") Date receivedAt,
                    @JsonProperty("flow_id") String flowId) {
        this.occuredAt = occuredAt;
        this.eid = eid;
        this.eventType = eventType;
        this.flowId = flowId;
        this.receivedAt = receivedAt;
    }

    public Date getOccuredAt() {
        return occuredAt;
    }

    public void setOccuredAt(Date occuredAt) {
        this.occuredAt = occuredAt;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Date getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    @Override
    public String toString() {
        return "{\"occuredAt\":" + occuredAt.getTime() + ", \"eid\":\"" + eid + "\", \"eventType\":\"" + eventType
                + "\", \"receivedAt\":" + receivedAt.getTime() + ", \"flowId\":\"" + flowId + "\"}";
    }

}
