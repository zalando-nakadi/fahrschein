package org.zalando.fahrschein.inmemory;

import org.zalando.fahrschein.PartitionManager;
import org.zalando.fahrschein.test.AbstractPartitionManagerTest;

public class InMemoryPartitionManagerTest extends AbstractPartitionManagerTest {
    private final PartitionManager partitionManager = new InMemoryPartitionManager();
    private final PartitionManager partitionManager2 = new InMemoryPartitionManager();

    @Override
    protected PartitionManager partitionManager() {
        return partitionManager;
    }

    @Override
    protected PartitionManager partitionManagerForAnotherConsumer() {
        return partitionManager2;
    }
}
