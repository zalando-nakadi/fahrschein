package org.zalando.fahrschein.domain;

public final class Cursor {
    private final String partition;
    private final String offset;

    public Cursor(String partition, String offset) {
        this.partition = partition;
        this.offset = offset;
    }

    public String getPartition() {
        return partition;
    }

    public String getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "Cursor{" +
                "partition='" + partition + '\'' +
                ", offset='" + offset + '\'' +
                '}';
    }
}
