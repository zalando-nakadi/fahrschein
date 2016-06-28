package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Partition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface PartitionManager {
    boolean lockPartition(String consumerName, String eventName, String partition, String lockedBy, long timeout, TimeUnit timeoutUnit);
    void unlockPartition(String consumerName, String eventName, String partition, String lockedBy);

    default List<Partition> lockPartitions(String consumerName, String eventName, List<Partition> partitions, String lockedBy, long timout, TimeUnit timeoutUnit) {
        final List<Partition> result = new ArrayList<>(partitions.size());

        for (Partition partition : partitions) {
            final boolean locked = lockPartition(consumerName, eventName, partition.getPartition(), lockedBy, timout, timeoutUnit);
            if (locked) {
                result.add(partition);
            }
        }

        return result;
    }
}
