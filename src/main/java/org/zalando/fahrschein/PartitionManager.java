package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface PartitionManager {
    /**
     *
     * @param eventName Name of the event
     * @param partitions The partitions to lock
     * @param lockedBy Name of one node of the consuming application
     * @param timeout Timeout after that the lock will automatically be released
     * @param timeoutUnit Time unit for the timeout
     * @return Whether locking succeeded, i.e. the lock was not held by anyone or the existing lock expired. The current implementations will also return true if the lock is already held by the same node, but not update the expiry time.
     */
    Optional<Lock> lockPartitions(String eventName, List<Partition> partitions, String lockedBy, long timeout, TimeUnit timeoutUnit);

    /**
     *
     * @param lock The lock object returned from {@link PartitionManager#lockPartitions(String, List, String, long, TimeUnit)}
     */
    boolean unlockPartitions(Lock lock);

}
