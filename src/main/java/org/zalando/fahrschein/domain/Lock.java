package org.zalando.fahrschein.domain;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Immutable
public class Lock {
    private final String consumerName;
    private final String eventName;
    private final String lockedBy;
    private final long lockTimeout;
    private final TimeUnit timeoutUnit;
    private final List<Partition> partitions;

    public Lock(String consumerName, String eventName, String lockedBy, long lockTimeout, TimeUnit timeoutUnit, List<Partition> partitions) {
        this.consumerName = consumerName;
        this.eventName = eventName;
        this.lockedBy = lockedBy;
        this.lockTimeout = lockTimeout;
        this.timeoutUnit = timeoutUnit;
        this.partitions = Collections.unmodifiableList(new ArrayList<>(partitions));
    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getEventName() {
        return eventName;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public long getLockTimeout() {
        return lockTimeout;
    }

    public TimeUnit getTimeoutUnit() {
        return timeoutUnit;
    }

    public List<Partition> getPartitions() {
        return partitions;
    }
}
