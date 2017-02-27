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
    private String eventType;
    private Date receivedAt;

    @JsonCreator
    public MetaData(@JsonProperty("occurred_at") Date occuredAt,
                    @JsonProperty("event_type") String eventType,
                    @JsonProperty("received_at") Date receivedAt) {
        this.occuredAt = occuredAt;
        this.eventType = eventType;
        this.receivedAt = receivedAt;
    }

    public Date getOccuredAt() {
        return occuredAt;
    }

    public String getEventType() {
        return eventType;
    }

    public Date getReceivedAt() {
        return receivedAt;
    }

}
