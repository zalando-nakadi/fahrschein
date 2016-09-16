package org.zalando.fahrschein;


import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class SimplePartitionManager implements PartitionManager {

    protected abstract boolean acquireLock(String eventName, String lockedBy, TimeUnit timeoutUnit);
    protected abstract void releaseLock(String eventName, String lockedBy);

    @Override
    public Optional<Lock> lockPartitions(String eventName, List<Partition> partitions, String lockedBy, long timeout, TimeUnit timeoutUnit) {
        if (acquireLock(eventName, lockedBy, timeoutUnit)) {
            return Optional.of(new Lock(eventName, lockedBy, timeout, timeoutUnit, partitions));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean unlockPartitions(Lock lock) {
        releaseLock(lock.getEventName(), lock.getLockedBy());
        return true;
    }
}
