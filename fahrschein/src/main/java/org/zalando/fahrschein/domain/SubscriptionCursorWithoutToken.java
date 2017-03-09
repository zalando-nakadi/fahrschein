package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class SubscriptionCursorWithoutToken {
    private final String partition;
    private final String offset;
    private final String eventType;

    @JsonCreator
    public SubscriptionCursorWithoutToken(String partition, String offset, String eventType) {
        this.partition = partition;
        this.offset = offset;
        this.eventType = eventType;
    }

    public SubscriptionCursorWithoutToken(String partition, String offset) {
        this.partition = partition;
        this.offset = offset;
        this.eventType = null;
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

    @Override
    public String toString() {
        return "Cursor{" +
                "partition='" + partition + '\'' +
                ", offset='" + offset + '\'' +
                ", eventType='" + eventType + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SubscriptionCursorWithoutToken cursor = (SubscriptionCursorWithoutToken) o;

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
