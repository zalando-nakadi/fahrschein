package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Partition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface PartitionManager {
    /**
     *
     * @param consumerName Name of the consuming application which might be running on multiple nodes
     * @param eventName Name of the event
     * @param partition The partition to lock
     * @param lockedBy Name of one node of the consuming application
     * @param timeout Timeout after that the lock will automatically be released
     * @param timeoutUnit Time unit for the timeout
     * @return Whether locking succeeded, i.e. the lock was not held by anyone or the existing lock expired. The current implementations will also return true if the lock is already held by the same node, but not update the expiry time.
     */
    boolean lockPartition(String consumerName, String eventName, String partition, String lockedBy, long timeout, TimeUnit timeoutUnit);

    /**
     *
     * @param consumerName Name of the consuming application which might be running on multiple nodes
     * @param eventName Name of the event
     * @param partition The partition to lock
     * @param lockedBy Name of one node of the consuming application
     */
    void unlockPartition(String consumerName, String eventName, String partition, String lockedBy);

    /**
     *
     * @param consumerName Name of the consuming application which might be running on multiple nodes
     * @param eventName Name of the event
     * @param partitions List of the partitions that should be locked
     * @param lockedBy Name of one node of the consuming application
     * @param timeout Timeout after that the lock will automatically be released
     * @param timeoutUnit Time unit for the timeout
     * @return List of the successfully locked partitions
     */
    default List<Partition> lockPartitions(String consumerName, String eventName, List<Partition> partitions, String lockedBy, long timeout, TimeUnit timeoutUnit) {
        final List<Partition> result = new ArrayList<>(partitions.size());

        for (Partition partition : partitions) {
            final boolean locked = lockPartition(consumerName, eventName, partition.getPartition(), lockedBy, timeout, timeoutUnit);
            if (locked) {
                result.add(partition);
            }
        }

        return result;
    }
}
