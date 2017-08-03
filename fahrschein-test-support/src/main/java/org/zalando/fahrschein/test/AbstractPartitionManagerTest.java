package org.zalando.fahrschein.test;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.fahrschein.PartitionManager;
import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import java.util.ArrayList;
import java.util.List;

// Looks like the Transactional annotation has to be on the class actually declaring the methods
@Transactional
public abstract class AbstractPartitionManagerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected abstract PartitionManager partitionManager();
    protected abstract PartitionManager partitionManagerForAnotherConsumer();

    protected List<Partition> partitions(String... ids) {
        final List<Partition> result = new ArrayList<>(ids.length);
        for (String id : ids) {
            result.add(new Partition(id, "0", "0"));
        }
        return result;
    }

    @Test
    public void shouldLock() {
        final Lock locked = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked.isLocked());
    }

    @Test
    public void shouldAllowLockBySameNode() {
        final Lock locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isLocked());

        final Lock locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked2.isLocked());
    }

    @Test
    public void shouldNotLockAlreadyLocked() {
        final Lock locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isLocked());

        final Lock locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-2");
        Assert.assertFalse(locked2.isLocked());
    }

    @Test
    public void shouldLockIndependentConsumers() {
        final Lock locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "consumer-1-node-1");
        Assert.assertTrue(locked1.isLocked());

        final Lock locked2 = partitionManagerForAnotherConsumer().lockPartitions("sales-order-placed", partitions("0"), "consumer-2-node-1");
        Assert.assertTrue(locked2.isLocked());
    }

    @Test
    public void shouldLockIndependentEvents() {
        final Lock locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isLocked());

        final Lock locked2 = partitionManager().lockPartitions("address-changed", partitions("0"), "node-1");
        Assert.assertTrue(locked2.isLocked());
    }

    @Test
    public void shouldLockIndependentPartitions() {
        final Lock locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isLocked());

        final Lock locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("1"), "node-1");
        Assert.assertTrue(locked2.isLocked());
    }

    @Test
    public void shouldUnlock() throws InterruptedException {
        final Lock locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isLocked());

        partitionManager().unlockPartitions(locked1);

        final Lock locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-2");
        Assert.assertTrue(locked2.isLocked());
    }

    @Test
    public void shouldFailOnInvalidUnlock() throws InterruptedException {
        final Lock locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isLocked());

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Could not unlock");

        partitionManager().unlockPartitions(new Lock("sales-order-placed", "node-2", partitions("0")));
    }

}
