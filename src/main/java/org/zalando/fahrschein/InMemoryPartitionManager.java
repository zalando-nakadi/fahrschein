package org.zalando.fahrschein;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPartitionManager extends SimplePartitionManager {
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

        LockInfo(String lockedBy) {
            this.lockedBy = lockedBy;
        }
    }

    private final ConcurrentHashMap<LockKey, LockInfo> locks = new ConcurrentHashMap<>();

    @Override
    protected boolean acquireLock(String eventName, String lockedBy) {
        final LockKey lockKey = new LockKey(eventName);
        final LockInfo tryLock = new LockInfo(lockedBy);
        final LockInfo newLock = locks.compute(lockKey, (key, oldLock) -> oldLock == null ? tryLock : oldLock);
        return lockedBy.equals(newLock.lockedBy);
    }

    @Override
    protected void releaseLock(String eventName, String lockedBy) {
        final LockKey lockKey = new LockKey(eventName);
        final LockInfo newLock = locks.compute(lockKey, (key, old) -> old != null && old.lockedBy.equals(lockedBy) ? null : old);
        if (newLock != null) {
            throw new IllegalStateException("Could not unlock [" +eventName + "] by [" + lockedBy + "] because it is locked by [" + newLock.lockedBy + "]");
        }
    }

}
