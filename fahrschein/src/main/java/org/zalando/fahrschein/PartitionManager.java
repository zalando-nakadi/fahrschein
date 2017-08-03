package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import java.util.List;

public interface PartitionManager {
    /**
     * Locks partitions for one event. When running consumers on multiple nodes they should try to lock partitions so that events are only processed by one consumer.
     *
     * @param eventName Name of the event
     * @param partitions The partitions to lock
     * @param lockedBy Name of one node of the consuming application
     * @return A {@link Lock} instance containing the list of locked partitions if locking succeeded, or containing an empty list otherwise
     */
    Lock lockPartitions(String eventName, List<Partition> partitions, String lockedBy);

    /**
     * Unlocks previously locked partitions.
     *
     * @param lock The lock object returned from {@link PartitionManager#lockPartitions(String, List, String)}
     */
    void unlockPartitions(Lock lock);

}
