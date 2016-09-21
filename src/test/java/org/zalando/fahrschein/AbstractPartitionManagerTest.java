package org.zalando.fahrschein;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

// Looks like the Transactional annotation has to be on the class actually declaring the methods
@Transactional
public abstract class AbstractPartitionManagerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected abstract PartitionManager partitionManager();
    protected abstract PartitionManager partitionManagerForAnotherConsumer();

    protected List<Partition> partitions(String... ids) {
        return Arrays.stream(ids).map(id -> new Partition(id, "0", "0")).collect(toList());
    }

    @Test
    public void shouldLock() {
        final Optional<Lock> locked = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked.isPresent());
    }

    @Test
    public void shouldAllowLockBySameNode() {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isPresent());

        final Optional<Lock> locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked2.isPresent());
    }

    @Test
    public void shouldNotLockAlreadyLocked() {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isPresent());

        final Optional<Lock> locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-2");
        Assert.assertFalse(locked2.isPresent());
    }

    @Test
    public void shouldLockIndependentConsumers() {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "consumer-1-node-1");
        Assert.assertTrue(locked1.isPresent());

        final Optional<Lock> locked2 = partitionManagerForAnotherConsumer().lockPartitions("sales-order-placed", partitions("0"), "consumer-2-node-1");
        Assert.assertTrue(locked2.isPresent());
    }

    @Test
    public void shouldLockIndependentEvents() {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isPresent());

        final Optional<Lock> locked2 = partitionManager().lockPartitions("address-changed", partitions("0"), "node-1");
        Assert.assertTrue(locked2.isPresent());
    }

    @Test
    public void shouldLockIndependentPartitions() {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isPresent());

        final Optional<Lock> locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("1"), "node-1");
        Assert.assertTrue(locked2.isPresent());
    }

    @Test
    public void shouldUnlock() throws InterruptedException {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isPresent());

        partitionManager().unlockPartitions(locked1.get());

        final Optional<Lock> locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-2");
        Assert.assertTrue(locked2.isPresent());
    }

    @Test
    public void shouldFailOnInvalidUnlock() throws InterruptedException {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        Assert.assertTrue(locked1.isPresent());

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Could not unlock");

        partitionManager().unlockPartitions(new Lock("sales-order-placed", "node-2", partitions("0")));
    }

}
