package org.zalando.fahrschein;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StreamParameters {

    private static final int DEFAULT_BATCH_LIMIT = 1;

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
    // Only used in the subscription api
    @Nullable
    private final Integer maxUncommittedEvents;
    @Nullable
    private final Integer commitTimeout;


    private StreamParameters(@Nullable Integer batchLimit, @Nullable Integer streamLimit, @Nullable Integer batchFlushTimeout, @Nullable Integer streamTimeout, @Nullable Integer streamKeepAliveLimit, @Nullable Integer maxUncommittedEvents, @Nullable Integer commitTimeout) {
        this.batchLimit = batchLimit;
        this.streamLimit = streamLimit;
        this.batchFlushTimeout = batchFlushTimeout;
        this.streamTimeout = streamTimeout;
        this.streamKeepAliveLimit = streamKeepAliveLimit;
        this.maxUncommittedEvents = maxUncommittedEvents;
        this.commitTimeout = commitTimeout;
    }

    public StreamParameters() {
        this(null, null, null, null, null, null, null);
    }

    String toQueryString() {
        final List<String> params = new ArrayList<>(6);

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
        if (maxUncommittedEvents != null) {
            params.add("max_uncommitted_events=" + maxUncommittedEvents);
        }
        if (commitTimeout != null) {
            params.add("commit_timeout=" + commitTimeout);
        }

        return params.stream().collect(Collectors.joining("&"));
    }

    /**
     *
     * Maximum number of Events in each chunk (and therefore per partition) of the stream.
     *
     *
     * <p>
     *  Note 2017/05/19: the API definition says if the value is  0 or unspecified the server will
     *  buffer events indefinitely and flush on reaching of {@link StreamParameters#batchFlushTimeout}.
     *  This is incorrect - if the server receives a value of '0' it will not send events at
     *  all (effectively it's a silent bug). Because of this if value is set to 0 (or less than 1)
     *  client raise an exception.
     * </p>
     *
     * @param batchLimit
     *          batch_limit must be lower or equal to stream_limit
     * @throws IllegalArgumentException
     */
    public StreamParameters withBatchLimit(int batchLimit) throws IllegalArgumentException {
        if(streamLimit != null && streamLimit < batchLimit){
            throw new IllegalArgumentException("streamLimit is lower than batch_limit.");
        }
        if(batchLimit < DEFAULT_BATCH_LIMIT){
            throw new IllegalArgumentException("batch_limit can't be lower than 1.");
        }
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout);
    }

    /**
     *
     * Maximum number of Events in this stream (over all partitions being streamed in this connection).
     *
     * @param streamLimit
     *          If 0 or undefined, will stream batches indefinitely.
     *          Stream initialization will fail if stream_limit is lower than batch_limit.
     *
     * @throws IllegalArgumentException
     */
    public StreamParameters withStreamLimit(int streamLimit) throws IllegalArgumentException {
        if(batchLimit != null && batchLimit > streamLimit){
            throw new IllegalArgumentException("streamLimit is lower than batch_limit.");
        }
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout);
    }

    /**
     * Maximum time in seconds to wait for the flushing of each chunk (per partition).
     *
     * @param batchFlushTimeout
     *          If the amount of buffered Events reaches batch_limit before this batch_flush_timeout is reached,
     *          the messages are immediately flushed to the client and batch flush timer is reset.
     *          If 0 or undefined, will assume 30 seconds.
     *
     * @throws IllegalArgumentException
     */
    public StreamParameters withBatchFlushTimeout(int batchFlushTimeout) throws IllegalArgumentException {
        if (streamTimeout != null && streamTimeout < batchFlushTimeout){
            throw new IllegalArgumentException("stream_timeout is lower than batch_flush_timeout.");
        }

        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout);
    }

    /**
     * Maximum time in seconds a stream will live before connection is closed by the server. If 0 or unspecified will stream indefinitely.
     * If this timeout is reached, any pending messages (in the sense of stream_limit) will be flushed to the client.
     *
     * @param streamTimeout
     *          Stream initialization will fail if stream_timeout is lower than batch_flush_timeout
     * @throws IllegalArgumentException
     */
    public StreamParameters withStreamTimeout(int streamTimeout) throws IllegalArgumentException {
        if(batchFlushTimeout != null && batchFlushTimeout > streamTimeout){
            throw new IllegalArgumentException("stream_timeout is lower than batch_flush_timeout.");
        }
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout);
    }

    /**
     * Maximum amount of seconds that nakadi will be waiting for commit after sending a batch to a client.
     * If the commit does not come within this timeout, nakadi will initialize stream termination, no
     * new data will be sent. Partitions from this stream will be assigned to other streams.
     *
     * @param commitTimeout
     */
    public StreamParameters withCommitTimeout(int commitTimeout) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout);
    }

    /**
     *
     * Maximum number of empty keep alive batches to get in a row before closing the connection.
     *
     * @param streamKeepAliveLimit
     *          If 0 or undefined will send keep alive messages indefinitely.
     */
    public StreamParameters withStreamKeepAliveLimit(int streamKeepAliveLimit) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout);
    }

    public StreamParameters withMaxUncommittedEvents(int maxUncommittedEvents) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout);
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

    public Optional<Integer> getMaxUncommittedEvents() {
        return Optional.ofNullable(maxUncommittedEvents);
    }

    public Optional<Integer> getCommitTimeout() {
        return Optional.ofNullable(commitTimeout);
    }

}
