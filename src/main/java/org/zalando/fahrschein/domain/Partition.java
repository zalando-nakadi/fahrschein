package org.zalando.fahrschein.domain;

public class Partition {
    private final String partition;
    private final String oldestAvailableOffset;
    private final String newestAvailableOffset;

    public Partition(String partition, String oldestAvailableOffset, String newestAvailableOffset) {
        this.partition = partition;
        this.oldestAvailableOffset = oldestAvailableOffset;
        this.newestAvailableOffset = newestAvailableOffset;
    }

    public String getPartition() {
        return partition;
    }

    public String getOldestAvailableOffset() {
        return oldestAvailableOffset;
    }

    public String getNewestAvailableOffset() {
        return newestAvailableOffset;
    }

}
