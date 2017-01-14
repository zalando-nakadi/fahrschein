package org.zalando.fahrschein.domain;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.zalando.fahrschein.Preconditions.checkState;

@Immutable
public final class Lock {
    private final String eventName;
    private final String lockedBy;
    private final List<Partition> partitions;

    public Lock(String eventName, String lockedBy, List<Partition> partitions) {
        checkState(!partitions.isEmpty(), "Locked partitions should not be empty");
        this.eventName = eventName;
        this.lockedBy = lockedBy;
        this.partitions = Collections.unmodifiableList(new ArrayList<>(partitions));
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
