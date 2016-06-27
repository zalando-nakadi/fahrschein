package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Partition;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class InMemoryPartitionManager implements PartitionManager {
    static final class LockKey {
        private final String consumerName;
        private final String eventName;
        private final String partition;

        LockKey(String consumerName, String eventName, String partition) {
            this.consumerName = consumerName;
            this.eventName = eventName;
            this.partition = partition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LockKey lockKey = (LockKey) o;
            return Objects.equals(consumerName, lockKey.consumerName) &&
                    Objects.equals(eventName, lockKey.eventName) &&
                    Objects.equals(partition, lockKey.partition);
        }

        @Override
        public int hashCode() {
            return Objects.hash(consumerName, eventName, partition);
        }
    }

    static final class LockInfo {
        private final String lockedBy;
        private final long lockedUntil;

        LockInfo(String lockedBy, long lockedUntil) {
            this.lockedBy = lockedBy;
            this.lockedUntil = lockedUntil;
        }
    }

    private final ConcurrentHashMap<LockKey, LockInfo> locks = new ConcurrentHashMap<>();

    @Override
    public List<Partition> getPartitions() {
        return null;
    }

    @Override
    public boolean lockPartition(String consumerName, String eventName, String partition, String lockedBy, long timeout, TimeUnit timeoutUnit) {
        long now = System.currentTimeMillis();
        final LockInfo tryLock = new LockInfo(lockedBy, now + timeoutUnit.toMillis(timeout));
        final LockKey lockKey = new LockKey(consumerName, eventName, partition);
        final LockInfo newLock = locks.compute(lockKey, (key, old) -> old == null || old.lockedUntil < now ? tryLock : old);
        return newLock == tryLock;
    }

    @Override
    public void unlockPartition(String consumerName, String eventName, String partition, String lockedBy) {
        final LockKey lockKey = new LockKey(consumerName, eventName, partition);
        final LockInfo newLock = locks.compute(lockKey, (key, old) -> old != null && old.lockedBy.equals(lockedBy) ? null : old);
        if (newLock != null) {
            throw new IllegalStateException("Consumer [" + consumerName + "] tried to unlock partition locked by [" + newLock.lockedBy + "] instead of [" + lockedBy + "]");
        }
    }
}
