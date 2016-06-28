package org.zalando.fahrschein.salesorder;

import org.zalando.fahrschein.InMemoryPartitionManager;
import org.zalando.fahrschein.PartitionManager;

public class InMemoryPartitionManagerTest extends AbstractPartitionManagerTest {
    private final PartitionManager partitionManager = new InMemoryPartitionManager();

    @Override
    protected PartitionManager partitionManager() {
        return partitionManager;
    }
}
