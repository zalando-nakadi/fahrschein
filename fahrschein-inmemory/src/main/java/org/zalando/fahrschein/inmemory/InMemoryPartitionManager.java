package org.zalando.fahrschein.inmemory;

import org.zalando.fahrschein.SimplePartitionManager;

import java.util.HashMap;
import java.util.Map;

public class InMemoryPartitionManager extends SimplePartitionManager {

    private final Map<String, String> locks = new HashMap<>();

    @Override
    protected boolean acquireLock(String eventName, String lockedBy) {
        synchronized (locks) {
            final String prev = locks.get(eventName);
            if (prev == null) {
                locks.put(eventName, lockedBy);
                return true;
            } else {
                return prev.equals(lockedBy);
            }
        }
    }

    @Override
    protected void releaseLock(String eventName, String lockedBy) {
        synchronized (locks) {
            final String prev = locks.get(eventName);
            if (prev == null || !prev.equals(lockedBy)) {
                throw new IllegalStateException("Could not unlock [" +eventName + "] by [" + lockedBy + "] because it is locked by [" + prev + "]");
            } else {
                locks.remove(eventName);
            }
        }
    }

}
