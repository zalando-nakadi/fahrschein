package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class Cursor {
    private final String partition;
    private final String offset;
    @Nullable
    private final String eventType;
    @Nullable
    private final String cursorToken;

    @JsonCreator
    public Cursor(@JsonProperty("partition") String partition, @JsonProperty("offset") String offset, @Nullable @JsonProperty("event_type") String eventType, @Nullable @JsonProperty("cursor_token") String cursorToken) {
        this.partition = partition;
        this.offset = offset;
        this.eventType = eventType;
        this.cursorToken = cursorToken;
    }

    public Cursor(String partition, String offset, String eventType) {
        this.partition = partition;
        this.offset = offset;
        this.eventType = eventType;
        this.cursorToken = null;
    }

    public Cursor(String partition, String offset) {
        this.partition = partition;
        this.offset = offset;
        this.eventType = null;
        this.cursorToken = null;
    }

    public String getPartition() {
        return partition;
    }

    public String getOffset() {
        return offset;
    }

    /**
     * The event type of this cursor. Only available if the batch was received using the subscription api.
     */
    @Nullable
    public String getEventType() {
        return eventType;
    }

    /**
     * A token identifying this cursor and offset which has to be used when committing using the subscription api.
     * Only available if the batch was received using the subscription api.
     */
    @Nullable
    public String getCursorToken() {
        return cursorToken;
    }

    @Override
    public String toString() {
        return "Cursor{" +
                "partition='" + partition + '\'' +
                ", offset='" + offset + '\'' +
                ", eventType='" + eventType + '\'' +
                ", cursorToken='" + cursorToken + '\'' +
                '}';
    }

}
