package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toSet;

public class InMemoryPartitionManager implements PartitionManager {
    static final class LockKey {
        private final String eventName;

        LockKey(String eventName) {
            this.eventName = eventName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LockKey lockKey = (LockKey) o;
            return Objects.equals(eventName, lockKey.eventName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventName);
        }
    }

    static final class LockInfo {
        private final String lockedBy;
        private final long lockedUntil;
        private final Set<String> partitionIds;

        LockInfo(String lockedBy, long lockedUntil, Set<String> partitionIds) {
            this.lockedBy = lockedBy;
            this.lockedUntil = lockedUntil;
            this.partitionIds = partitionIds;
        }
    }

    private final ConcurrentHashMap<LockKey, LockInfo> locks = new ConcurrentHashMap<>();

    @Override
    public Optional<Lock> lockPartitions(String eventName, List<Partition> partitions, String lockedBy, long timeout, TimeUnit timeoutUnit) {
        long now = System.currentTimeMillis();
        final Set<String> partitionIds = partitions.stream().map(Partition::getPartition).collect(toSet());
        final LockKey lockKey = new LockKey(eventName);
        final LockInfo tryLock = new LockInfo(lockedBy, now + timeoutUnit.toMillis(timeout), partitionIds);
        final LockInfo newLock = locks.compute(lockKey, (key, oldLock) -> oldLock == null || oldLock.lockedUntil < now ? tryLock : oldLock);
        return lockedBy.equals(newLock.lockedBy) ? Optional.of(new Lock(eventName, lockedBy, timeout, timeoutUnit, partitions)) : Optional.<Lock>empty();
    }

    @Override
    public boolean unlockPartitions(Lock lock) {
        final String lockedBy = lock.getLockedBy();
        final LockKey lockKey = new LockKey(lock.getEventName());
        final LockInfo newLock = locks.compute(lockKey, (key, old) -> old != null && old.lockedBy.equals(lockedBy) ? null : old);
        return newLock == null;
    }
}
