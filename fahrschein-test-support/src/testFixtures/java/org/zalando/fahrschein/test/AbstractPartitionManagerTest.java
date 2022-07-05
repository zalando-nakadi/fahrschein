package org.zalando.fahrschein.test;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.fahrschein.PartitionManager;
import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Looks like the Transactional annotation has to be on the class actually declaring the methods
@Transactional
public abstract class AbstractPartitionManagerTest {

    protected abstract PartitionManager partitionManager();
    protected abstract PartitionManager partitionManagerForAnotherConsumer();

    protected List<Partition> partitions(String... ids) {
        return Arrays.stream(ids).map(id -> new Partition(id, "0", "0")).collect(toList());
    }

    @Test
    public void shouldLock() {
        final Optional<Lock> locked = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        assertTrue(locked.isPresent());
    }

    @Test
    public void shouldAllowLockBySameNode() {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        assertTrue(locked1.isPresent());

        final Optional<Lock> locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        assertTrue(locked2.isPresent());
    }

    @Test
    public void shouldNotLockAlreadyLocked() {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        assertTrue(locked1.isPresent());

        final Optional<Lock> locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-2");
        assertFalse(locked2.isPresent());
    }

    @Test
    public void shouldLockIndependentConsumers() {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "consumer-1-node-1");
        assertTrue(locked1.isPresent());

        final Optional<Lock> locked2 = partitionManagerForAnotherConsumer().lockPartitions("sales-order-placed", partitions("0"), "consumer-2-node-1");
        assertTrue(locked2.isPresent());
    }

    @Test
    public void shouldLockIndependentEvents() {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        assertTrue(locked1.isPresent());

        final Optional<Lock> locked2 = partitionManager().lockPartitions("address-changed", partitions("0"), "node-1");
        assertTrue(locked2.isPresent());
    }

    @Test
    public void shouldLockIndependentPartitions() {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        assertTrue(locked1.isPresent());

        final Optional<Lock> locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("1"), "node-1");
        assertTrue(locked2.isPresent());
    }

    @Test
    public void shouldUnlock() throws InterruptedException {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        assertTrue(locked1.isPresent());

        partitionManager().unlockPartitions(locked1.get());

        final Optional<Lock> locked2 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-2");
        assertTrue(locked2.isPresent());
    }

    @Test
    public void shouldFailOnInvalidUnlock() throws InterruptedException {
        final Optional<Lock> locked1 = partitionManager().lockPartitions("sales-order-placed", partitions("0"), "node-1");
        assertTrue(locked1.isPresent());

        IllegalStateException expectedException = assertThrows(IllegalStateException.class, () -> {
            partitionManager().unlockPartitions(new Lock("sales-order-placed", "node-2", partitions("0")));
        });

        assertEquals("Could not unlock [sales-order-placed] by [node-2] because it is locked by [node-1]", expectedException.getMessage());

    }

}
