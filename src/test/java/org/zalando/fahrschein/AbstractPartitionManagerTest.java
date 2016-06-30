package org.zalando.fahrschein;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

// Looks like the Transactional annotation has to be on the class actually declaring the methods
@Transactional
public abstract class AbstractPartitionManagerTest {

    protected abstract PartitionManager partitionManager();

    @Test
    public void shouldLock() {
        boolean locked = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked);
    }

    @Test
    public void shouldAllowLockBySameNode() {
        boolean locked1 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked1);

        boolean locked2 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked2);
    }

    @Test
    public void shouldNotLockAlreadyLocked() {
        boolean locked1 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked1);

        boolean locked2 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-2", 1, TimeUnit.HOURS);
        Assert.assertFalse(locked2);
    }

    @Test
    public void shouldLockIndependentConsumers() {
        boolean locked1 = partitionManager().lockPartition("test1", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked1);

        boolean locked2 = partitionManager().lockPartition("test2", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked2);
    }

    @Test
    public void shouldLockIndependentEvents() {
        boolean locked1 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked1);

        boolean locked2 = partitionManager().lockPartition("test", "address-changed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked2);
    }

    @Test
    public void shouldLockIndependentPartitions() {
        boolean locked1 = partitionManager().lockPartition("test1", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked1);

        boolean locked2 = partitionManager().lockPartition("test2", "sales-order-placed", "1", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked2);
    }

    @Test
    @Transactional
    public void shouldExpireLocks() throws InterruptedException {
        boolean locked1 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.MILLISECONDS);
        Assert.assertTrue(locked1);

        Thread.sleep(50);

        boolean locked2 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-2", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked2);
    }

    @Test
    public void shouldUnlock() throws InterruptedException {
        boolean locked1 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked1);

        partitionManager().unlockPartition("test", "sales-order-placed", "0", "node-1");

        boolean locked2 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-2", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked2);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOnInvalidUnlock() throws InterruptedException {
        boolean locked1 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked1);

        partitionManager().unlockPartition("test", "sales-order-placed", "0", "node-2");
    }

}
