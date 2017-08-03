package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Partition {
    private final String partition;
    private final String oldestAvailableOffset;
    private final String newestAvailableOffset;

    @JsonCreator
    public Partition(@JsonProperty("partition") final String partition,
                     @JsonProperty("oldest_available_offset") final String oldestAvailableOffset,
                     @JsonProperty("newest_available_offset") final String newestAvailableOffset) {
        this.partition = partition;
        this.oldestAvailableOffset = oldestAvailableOffset;
        this.newestAvailableOffset = newestAvailableOffset;
    }

    public String getPartition() {
        return partition;
    }

    public String getOldestAvailableOffset() {
        return oldestAvailableOffset;
    }

    public String getNewestAvailableOffset() {
        return newestAvailableOffset;
    }

}
