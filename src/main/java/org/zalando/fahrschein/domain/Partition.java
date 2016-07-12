package org.zalando.fahrschein.domain;

import com.google.gag.annotation.remark.Hack;

import javax.annotation.concurrent.Immutable;

@Immutable
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

    @Hack("This method relies on offsets being numeric and montonically increasing")
    public boolean isAvailable(final String offset) {
        try {
            final long requestedOffset = Long.parseLong(offset);
            final long oldestAvailableOffset = Long.parseLong(this.oldestAvailableOffset);
            final long newestAvailableOffset = Long.parseLong(this.newestAvailableOffset);

            return requestedOffset >= oldestAvailableOffset;
        } catch (NumberFormatException e) {
            // Assume it is available and wait for the problem response from nakadi
            return true;
        }
    }


}
