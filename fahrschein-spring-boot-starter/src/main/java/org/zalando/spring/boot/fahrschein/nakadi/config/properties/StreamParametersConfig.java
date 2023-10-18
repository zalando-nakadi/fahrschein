package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.Data;
import org.zalando.spring.boot.fahrschein.config.TimeSpan;

import java.util.Optional;

@Data
public class StreamParametersConfig {
    private Integer batchLimit;
    private Integer streamLimit;
    private TimeSpan batchFlushTimeout;
    private TimeSpan streamTimeout;
    private TimeSpan streamKeepAliveLimit;
    private TimeSpan commitTimeout;
    private TimeSpan batchTimespan;

    // Only used in the subscription api
    private Integer maxUncommittedEvents;

    public void mergeFromDefaults(StreamParametersConfig defaultStreamParametersConfig) {
        this.setBatchLimit(Optional.ofNullable(this.getBatchLimit()).orElse(defaultStreamParametersConfig.getBatchLimit()));
        this.setStreamLimit(Optional.ofNullable(this.getStreamLimit()).orElse(defaultStreamParametersConfig.getStreamLimit()));
        this.setBatchFlushTimeout(Optional.ofNullable(this.getBatchFlushTimeout()).orElse(defaultStreamParametersConfig.getBatchFlushTimeout()));
        this.setStreamTimeout(Optional.ofNullable(this.getStreamTimeout()).orElse(defaultStreamParametersConfig.getStreamTimeout()));
        this.setStreamKeepAliveLimit(Optional.ofNullable(this.getStreamKeepAliveLimit()).orElse(defaultStreamParametersConfig.getStreamKeepAliveLimit()));
        this.setCommitTimeout(Optional.ofNullable(this.getCommitTimeout()).orElse(defaultStreamParametersConfig.getCommitTimeout()));
        this.setBatchTimespan(Optional.ofNullable(this.getBatchTimespan()).orElse(defaultStreamParametersConfig.getBatchTimespan()));
        this.setMaxUncommittedEvents(Optional.ofNullable(this.getMaxUncommittedEvents()).orElse(defaultStreamParametersConfig.getMaxUncommittedEvents()));
    }

}
