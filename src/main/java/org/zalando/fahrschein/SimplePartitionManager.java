package org.zalando.fahrschein;


import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import java.util.List;
import java.util.Optional;

public abstract class SimplePartitionManager implements PartitionManager {

    protected abstract boolean acquireLock(String eventName, String lockedBy);
    protected abstract void releaseLock(String eventName, String lockedBy);

    @Override
    public Optional<Lock> lockPartitions(String eventName, List<Partition> partitions, String lockedBy) {
        if (acquireLock(eventName, lockedBy)) {
            return Optional.of(new Lock(eventName, lockedBy, partitions));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void unlockPartitions(Lock lock) {
        releaseLock(lock.getEventName(), lock.getLockedBy());
    }
}
