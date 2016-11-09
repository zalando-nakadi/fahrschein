package org.zalando.fahrschein.domain;

import com.google.gag.annotation.remark.Hack;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class Cursor {
    private final String partition;
    private final String offset;
    private final String eventType;
    private final String cursorToken;

    @Hack("Necessary to enable Jackson Wrapper working with multiple constructors but without @JsonCreator annotation")
    private Cursor() {
        this(null, null, null, null);
    }

    public Cursor(String partition, String offset, String eventType, String cursorToken) {
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
                ", eventType='" + eventType + '\'' +
                ", cursorToken='" + cursorToken + '\'' +
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
