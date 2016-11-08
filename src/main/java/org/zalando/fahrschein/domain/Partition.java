package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

import static org.zalando.fahrschein.domain.Partition.FieldNames.*;

@Immutable
public class Partition {

    class FieldNames {
        static final String OLDEST_AVAILABLE_OFFSET = "oldest_available_offset";
        static final String NEWEST_AVAILABLE_OFFSET = "newest_available_offset";
        static final String PARTITION = "partition";
    }

    private final String partition;

    @JsonProperty(OLDEST_AVAILABLE_OFFSET)
    private final String oldestAvailableOffset;

    @JsonProperty(NEWEST_AVAILABLE_OFFSET)
    private final String newestAvailableOffset;

    @JsonCreator
    public Partition(@JsonProperty(PARTITION) final String partition,
            @JsonProperty(OLDEST_AVAILABLE_OFFSET) final String oldestAvailableOffset,
            @JsonProperty(NEWEST_AVAILABLE_OFFSET) final String newestAvailableOffset) {
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
