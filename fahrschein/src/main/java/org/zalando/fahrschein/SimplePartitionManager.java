package org.zalando.fahrschein;


import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import java.util.Collections;
import java.util.List;

public abstract class SimplePartitionManager implements PartitionManager {

    protected abstract boolean acquireLock(String eventName, String lockedBy);
    protected abstract void releaseLock(String eventName, String lockedBy);

    @Override
    public Lock lockPartitions(String eventName, List<Partition> partitions, String lockedBy) {
        if (acquireLock(eventName, lockedBy)) {
            return new Lock(eventName, lockedBy, partitions);
        } else {
            return new Lock(eventName, lockedBy, Collections.<Partition>emptyList());
        }
    }

    @Override
    public void unlockPartitions(Lock lock) {
        releaseLock(lock.getEventName(), lock.getLockedBy());
    }
}
