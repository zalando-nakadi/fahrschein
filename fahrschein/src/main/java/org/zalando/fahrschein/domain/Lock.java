package org.zalando.fahrschein.domain;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Immutable
public final class Lock {
    private final String eventName;
    private final String lockedBy;
    private final List<Partition> partitions;

    public Lock(String eventName, String lockedBy, List<Partition> partitions) {
        this.eventName = eventName;
        this.lockedBy = lockedBy;
        this.partitions = Collections.unmodifiableList(new ArrayList<>(partitions));
    }

    public boolean isLocked() {
        return !partitions.isEmpty();
    }

    public String getEventName() {
        return eventName;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public List<Partition> getPartitions() {
        return partitions;
    }
}
