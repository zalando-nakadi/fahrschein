package org.zalando.fahrschein;

public class InMemoryPartitionManagerTest extends AbstractPartitionManagerTest {
    private final PartitionManager partitionManager = new InMemoryPartitionManager("test-consumer-1");
    private final PartitionManager partitionManager2 = new InMemoryPartitionManager("test-consumer-2");

    @Override
    protected PartitionManager partitionManager() {
        return partitionManager;
    }

    @Override
    protected PartitionManager partitionManagerForAnotherConsumer() {
        return partitionManager2;
    }
}
