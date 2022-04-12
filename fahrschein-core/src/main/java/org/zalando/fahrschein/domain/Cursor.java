package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public final class Cursor {
    private final String partition;
    private final String offset;
    @Nullable
    private final String eventType;
    @Nullable
    private final String cursorToken;

    @JsonCreator
    public Cursor(String partition, String offset, @Nullable String eventType, @Nullable String cursorToken) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cursor cursor = (Cursor) o;
        return Objects.equals(partition, cursor.partition) && Objects.equals(offset, cursor.offset) && Objects.equals(eventType, cursor.eventType) && Objects.equals(cursorToken, cursor.cursorToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partition, offset, eventType, cursorToken);
    }
}
