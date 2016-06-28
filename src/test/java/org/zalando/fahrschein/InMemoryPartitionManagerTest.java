package org.zalando.fahrschein;

public class InMemoryPartitionManagerTest extends AbstractPartitionManagerTest {
    private final PartitionManager partitionManager = new InMemoryPartitionManager();

    @Override
    protected PartitionManager partitionManager() {
        return partitionManager;
    }
}
