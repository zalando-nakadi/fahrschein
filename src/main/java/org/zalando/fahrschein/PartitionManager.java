package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Partition;

import java.util.List;

public interface PartitionManager {
    List<Partition> getPartitions();

    void lockPartition(String partition, long timeout);
    void unlockPartition(String partition);
}
