package org.zalando.fahrschein;

import com.google.common.base.Joiner;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StreamParameters {
    @Nullable
    private final Integer batchLimit;
    @Nullable
    private final Integer streamLimit;
    @Nullable
    private final Integer batchFlushTimeout;
    @Nullable
    private final Integer streamTimeout;
    @Nullable
    private final Integer streamKeepAliveLimit;

    private StreamParameters(Integer batchLimit, Integer streamLimit, Integer batchFlushTimeout, Integer streamTimeout, Integer streamKeepAliveLimit) {
        this.batchLimit = batchLimit;
        this.streamLimit = streamLimit;
        this.batchFlushTimeout = batchFlushTimeout;
        this.streamTimeout = streamTimeout;
        this.streamKeepAliveLimit = streamKeepAliveLimit;
    }

    public StreamParameters() {
        this(null, null, null, null, null);
    }

    String toQueryString() {
        final List<String> params = new ArrayList<>(5);

        if (batchLimit != null) {
            params.add("batch_limit=" + batchLimit);
        }
        if (streamLimit != null) {
            params.add("stream_limit=" + streamLimit);
        }
        if (batchFlushTimeout != null) {
            params.add("batch_flush_timeout=" + batchFlushTimeout);
        }
        if (streamTimeout != null) {
            params.add("stream_timeout=" + streamTimeout);
        }
        if (streamKeepAliveLimit != null) {
            params.add("stream_keep_alive_limit=" + streamKeepAliveLimit);
        }
        return Joiner.on("&").join(params);
    }

    public StreamParameters withBatchLimit(int batchLimit) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit);
    }

    public StreamParameters withStreamLimit(int streamLimit) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit);
    }

    public StreamParameters withBatchFlushTimeout(int batchFlushTimeout) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit);
    }

    public StreamParameters withStreamTimeout(int streamTimeout) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit);
    }

    public StreamParameters withStreamKeepAliveLimit(int streamKeepAliveLimit) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit);
    }

    public Optional<Integer> getBatchLimit() {
        return Optional.ofNullable(batchLimit);
    }

    public Optional<Integer> getStreamLimit() {
        return Optional.ofNullable(streamLimit);
    }

    public Optional<Integer> getBatchFlushTimeout() {
        return Optional.ofNullable(batchFlushTimeout);
    }

    public Optional<Integer> getStreamTimeout() {
        return Optional.ofNullable(streamTimeout);
    }

    public Optional<Integer> getStreamKeepAliveLimit() {
        return Optional.ofNullable(streamKeepAliveLimit);
    }
}
