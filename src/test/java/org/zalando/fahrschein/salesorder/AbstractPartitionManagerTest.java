package org.zalando.fahrschein.salesorder;

import org.junit.Assert;
import org.junit.Test;
import org.zalando.fahrschein.PartitionManager;

import java.util.concurrent.TimeUnit;

public abstract class AbstractPartitionManagerTest {

    protected abstract PartitionManager partitionManager();

    @Test
    public void shouldLock() {
        boolean locked = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked);
    }

    @Test
    public void shouldNotLockAlreadyLocked() {
        boolean locked1 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
        Assert.assertTrue(locked1);

        boolean locked2 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.HOURS);
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
    public void shouldExpireLocks() throws InterruptedException {
        boolean locked1 = partitionManager().lockPartition("test", "sales-order-placed", "0", "node-1", 1, TimeUnit.MILLISECONDS);
        Assert.assertTrue(locked1);

        Thread.sleep(10);

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
