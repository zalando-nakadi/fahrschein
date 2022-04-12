package org.zalando.fahrschein.domain;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Partition {
    private final String partition;
    private final String oldestAvailableOffset;
    private final String newestAvailableOffset;

    public Partition(final String partition,
                     final String oldestAvailableOffset,
                     final String newestAvailableOffset) {
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
