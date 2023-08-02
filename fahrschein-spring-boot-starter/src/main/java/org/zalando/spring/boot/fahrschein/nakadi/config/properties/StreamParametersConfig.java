package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import java.util.Optional;

public class StreamParametersConfig {
    private Integer batchLimit;
    private Integer streamLimit;
    private Integer batchFlushTimeout;
    private Integer streamTimeout;
    private Integer streamKeepAliveLimit;

    // Only used in the subscription api
    private Integer maxUncommittedEvents;

    public StreamParametersConfig() {
    }

    public void mergeFromDefaults(StreamParametersConfig defaultStreamParametersConfig) {
        this.setBatchLimit(Optional.ofNullable(this.getBatchLimit()).orElse(defaultStreamParametersConfig.getBatchLimit()));
        this.setStreamLimit(Optional.ofNullable(this.getStreamLimit()).orElse(defaultStreamParametersConfig.getStreamLimit()));
        this.setBatchFlushTimeout(Optional.ofNullable(this.getBatchFlushTimeout()).orElse(defaultStreamParametersConfig.getBatchFlushTimeout()));
        this.setStreamTimeout(Optional.ofNullable(this.getStreamTimeout()).orElse(defaultStreamParametersConfig.getStreamTimeout()));
        this.setStreamKeepAliveLimit(Optional.ofNullable(this.getStreamKeepAliveLimit()).orElse(defaultStreamParametersConfig.getStreamKeepAliveLimit()));
        this.setMaxUncommittedEvents(Optional.ofNullable(this.getMaxUncommittedEvents()).orElse(defaultStreamParametersConfig.getMaxUncommittedEvents()));
    }

    public Integer getBatchLimit() {
        return this.batchLimit;
    }

    public Integer getStreamLimit() {
        return this.streamLimit;
    }

    public Integer getBatchFlushTimeout() {
        return this.batchFlushTimeout;
    }

    public Integer getStreamTimeout() {
        return this.streamTimeout;
    }

    public Integer getStreamKeepAliveLimit() {
        return this.streamKeepAliveLimit;
    }

    public Integer getMaxUncommittedEvents() {
        return this.maxUncommittedEvents;
    }

    public void setBatchLimit(Integer batchLimit) {
        this.batchLimit = batchLimit;
    }

    public void setStreamLimit(Integer streamLimit) {
        this.streamLimit = streamLimit;
    }

    public void setBatchFlushTimeout(Integer batchFlushTimeout) {
        this.batchFlushTimeout = batchFlushTimeout;
    }

    public void setStreamTimeout(Integer streamTimeout) {
        this.streamTimeout = streamTimeout;
    }

    public void setStreamKeepAliveLimit(Integer streamKeepAliveLimit) {
        this.streamKeepAliveLimit = streamKeepAliveLimit;
    }

    public void setMaxUncommittedEvents(Integer maxUncommittedEvents) {
        this.maxUncommittedEvents = maxUncommittedEvents;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof StreamParametersConfig)) return false;
        final StreamParametersConfig other = (StreamParametersConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$batchLimit = this.getBatchLimit();
        final Object other$batchLimit = other.getBatchLimit();
        if (this$batchLimit == null ? other$batchLimit != null : !this$batchLimit.equals(other$batchLimit))
            return false;
        final Object this$streamLimit = this.getStreamLimit();
        final Object other$streamLimit = other.getStreamLimit();
        if (this$streamLimit == null ? other$streamLimit != null : !this$streamLimit.equals(other$streamLimit))
            return false;
        final Object this$batchFlushTimeout = this.getBatchFlushTimeout();
        final Object other$batchFlushTimeout = other.getBatchFlushTimeout();
        if (this$batchFlushTimeout == null ? other$batchFlushTimeout != null : !this$batchFlushTimeout.equals(other$batchFlushTimeout))
            return false;
        final Object this$streamTimeout = this.getStreamTimeout();
        final Object other$streamTimeout = other.getStreamTimeout();
        if (this$streamTimeout == null ? other$streamTimeout != null : !this$streamTimeout.equals(other$streamTimeout))
            return false;
        final Object this$streamKeepAliveLimit = this.getStreamKeepAliveLimit();
        final Object other$streamKeepAliveLimit = other.getStreamKeepAliveLimit();
        if (this$streamKeepAliveLimit == null ? other$streamKeepAliveLimit != null : !this$streamKeepAliveLimit.equals(other$streamKeepAliveLimit))
            return false;
        final Object this$maxUncommittedEvents = this.getMaxUncommittedEvents();
        final Object other$maxUncommittedEvents = other.getMaxUncommittedEvents();
        if (this$maxUncommittedEvents == null ? other$maxUncommittedEvents != null : !this$maxUncommittedEvents.equals(other$maxUncommittedEvents))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof StreamParametersConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $batchLimit = this.getBatchLimit();
        result = result * PRIME + ($batchLimit == null ? 43 : $batchLimit.hashCode());
        final Object $streamLimit = this.getStreamLimit();
        result = result * PRIME + ($streamLimit == null ? 43 : $streamLimit.hashCode());
        final Object $batchFlushTimeout = this.getBatchFlushTimeout();
        result = result * PRIME + ($batchFlushTimeout == null ? 43 : $batchFlushTimeout.hashCode());
        final Object $streamTimeout = this.getStreamTimeout();
        result = result * PRIME + ($streamTimeout == null ? 43 : $streamTimeout.hashCode());
        final Object $streamKeepAliveLimit = this.getStreamKeepAliveLimit();
        result = result * PRIME + ($streamKeepAliveLimit == null ? 43 : $streamKeepAliveLimit.hashCode());
        final Object $maxUncommittedEvents = this.getMaxUncommittedEvents();
        result = result * PRIME + ($maxUncommittedEvents == null ? 43 : $maxUncommittedEvents.hashCode());
        return result;
    }

    public String toString() {
        return "StreamParametersConfig(batchLimit=" + this.getBatchLimit() + ", streamLimit=" + this.getStreamLimit() + ", batchFlushTimeout=" + this.getBatchFlushTimeout() + ", streamTimeout=" + this.getStreamTimeout() + ", streamKeepAliveLimit=" + this.getStreamKeepAliveLimit() + ", maxUncommittedEvents=" + this.getMaxUncommittedEvents() + ")";
    }
}
