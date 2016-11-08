package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

import static org.zalando.fahrschein.domain.Cursor.FieldNames.*;

@Immutable
public final class Cursor {

    static class FieldNames {
        static final String EVENT_TYPE = "event_type";
        static final String CURSOR_TOKEN = "cursor_token";
        static final String PARTITION = "partition";
        static final String OFFSET = "offset";
    }

    private final String partition;
    private final String offset;
    @JsonProperty(EVENT_TYPE)
    private final String eventType;
    @JsonProperty(CURSOR_TOKEN)
    private final String cursorToken;

    @JsonCreator
    public Cursor(@JsonProperty(PARTITION) String partition, @JsonProperty(OFFSET) String offset, @JsonProperty(EVENT_TYPE) String eventType, @JsonProperty(CURSOR_TOKEN) String cursorToken) {
        this.partition = partition;
        this.offset = offset;
        this.eventType = eventType;
        this.cursorToken = cursorToken;
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

    public String getEventType() {
        return eventType;
    }

    public String getCursorToken() {
        return cursorToken;
    }

    @Override
    public String toString() {
        return "Cursor{" +
                "partition='" + partition + '\'' +
                ", offset='" + offset + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Cursor cursor = (Cursor) o;

        if (partition != null ? !partition.equals(cursor.partition) : cursor.partition != null) return false;
        return offset != null ? offset.equals(cursor.offset) : cursor.offset == null;

    }

    @Override
    public int hashCode() {
        int result = partition != null ? partition.hashCode() : 0;
        result = 31 * result + (offset != null ? offset.hashCode() : 0);
        return result;
    }
}
