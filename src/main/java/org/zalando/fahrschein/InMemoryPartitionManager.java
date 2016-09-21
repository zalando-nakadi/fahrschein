package org.zalando.fahrschein;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPartitionManager extends SimplePartitionManager {

    private final ConcurrentHashMap<String, String> locks = new ConcurrentHashMap<>();

    @Override
    protected boolean acquireLock(String eventName, String lockedBy) {
        final String newLockedBy = locks.compute(eventName, (key, oldLockedBy) -> oldLockedBy == null || oldLockedBy.equals(lockedBy) ? lockedBy : oldLockedBy);
        return lockedBy.equals(newLockedBy);
    }

    @Override
    protected void releaseLock(String eventName, String lockedBy) {
        final String newLockedBy = locks.compute(eventName, (key, oldLockedBy) -> oldLockedBy != null && oldLockedBy.equals(lockedBy) ? null : oldLockedBy);
        if (newLockedBy != null) {
            throw new IllegalStateException("Could not unlock [" +eventName + "] by [" + lockedBy + "] because it is locked by [" + newLockedBy + "]");
        }
    }

}
