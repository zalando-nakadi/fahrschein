package org.zalando.fahrschein;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    // Only used in the subscription api
    @Nullable
    private final Integer maxUncommittedEvents;
    @Nullable
    private final Integer commitTimeout;
    @Nullable
    private final Integer batchTimespan;


    private StreamParameters(@Nullable Integer batchLimit, @Nullable Integer streamLimit, @Nullable Integer batchFlushTimeout, @Nullable Integer streamTimeout, @Nullable Integer streamKeepAliveLimit, @Nullable Integer maxUncommittedEvents, @Nullable Integer commitTimeout, @Nullable Integer batchTimespan) {
        this.batchLimit = batchLimit;
        this.streamLimit = streamLimit;
        this.batchFlushTimeout = batchFlushTimeout;
        this.streamTimeout = streamTimeout;
        this.streamKeepAliveLimit = streamKeepAliveLimit;
        this.maxUncommittedEvents = maxUncommittedEvents;
        this.commitTimeout = commitTimeout;
        this.batchTimespan = batchTimespan;
    }

    public StreamParameters() {
        this(null, null, null, null, null, null, null, null);
    }

    String toQueryString() {
        final List<String> params = new ArrayList<>(8);

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
        if (batchTimespan != null) {
            params.add("batch_timespan=" + batchTimespan);
        }

        return params.stream().collect(Collectors.joining("&"));
    }

    /**
     *
     * Maximum number of events in each chunk (and therefore per partition) of the stream.
     *
     * @param batchLimit
     *          Nakadi's default value (at the time of writing): 1.
     *          batchLimit must be lower or equal to streamLimit. A value of 0 (or less than 1) is rejected by Nakadi.
     */
    public StreamParameters withBatchLimit(int batchLimit) throws IllegalArgumentException {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout, batchTimespan);
    }

    /**
     *
     * Maximum number of events in this stream (over all partitions being streamed in this connection).
     *
     * @param streamLimit
     *          If 0 or undefined, will stream batches indefinitely.
     *          Stream initialization will fail if streamLimit is lower than batchLimit.
     */
    public StreamParameters withStreamLimit(int streamLimit) throws IllegalArgumentException {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout, batchTimespan);
    }

    /**
     * <p>Maximum time in seconds to wait for the flushing of each chunk (per partition).</p>
     *
     * <p>If the amount of buffered Events reaches batchLimit before this batchFlushTimeout
     * is reached, the messages are immediately flushed to the client and batch flush timer is reset.</p>
     *
     * <p>If 0 or undefined, will assume 30 seconds.</p>
     *
     * <p>Value is treated as a recommendation. Nakadi may flush chunks with a smaller timeout.</p>
     *
     * @param batchFlushTimeout Maximum time in seconds to wait for the flushing of each chunk (per partition).
     *                          Nakadi's default value (at the time of writing): 30
     *
     */
    public StreamParameters withBatchFlushTimeout(int batchFlushTimeout) throws IllegalArgumentException {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout, batchTimespan);
    }

    /**
     * <p>Maximum time in seconds a stream will live before connection is closed by the server.
     * If 0 or unspecified will stream for 1h ±10min.</p>
     *
     * <p>If this timeout is reached, any pending messages (in the sense of stream_limit) will be flushed
     * to the client.</p>
     *
     * <p>Stream initialization will fail if streamTimeout is lower than batchFlushTimeout.
     * If the streamTimeout is greater than max value (4200 seconds) - Nakadi will treat this as not
     * specifying streamTimeout (this is done due to backwards compatibility).</p>
     *
     * @param streamTimeout Maximum time in seconds a stream will live before connection is closed by the server.
     *                      Nakadi's default value (at the time of writing): 0
     *          Stream initialization will fail if streamTimeout is lower than batchFlushTimeout
     */
    public StreamParameters withStreamTimeout(int streamTimeout) throws IllegalArgumentException {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout, batchTimespan);
    }

    /**
     * Maximum amount of seconds that Nakadi will be waiting for commit after sending a batch to a client.
     * In case if commit does not come within this timeout, Nakadi will initialize stream termination, no
     * new data will be sent. Partitions from this stream will be assigned to other streams.
     * Setting commitTimeout to 0 is equal to setting it to the maximum allowed value: 60 seconds.
     *
     * @param commitTimeout Maximum amount of seconds that Nakadi will be waiting for commit after sending a
     *                      batch to a client.
     */
    public StreamParameters withCommitTimeout(int commitTimeout) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout, batchTimespan);
    }

    /**
     *
     * Maximum number of empty keep alive batches to get in a row before closing the connection.
     *
     * @param streamKeepAliveLimit
     *          If 0 or undefined will send keep alive messages indefinitely.
     *          Nakadi's default value (at the time of writing): 0
     */
    public StreamParameters withStreamKeepAliveLimit(int streamKeepAliveLimit) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout, batchTimespan);
    }

    /**
     * The maximum number of uncommitted events that Nakadi will stream before pausing the stream. When in
     * paused state and commit comes - the stream will resume.
     *
     * @param maxUncommittedEvents Nakadi's default value (at the time of writing): 10
     */
    public StreamParameters withMaxUncommittedEvents(int maxUncommittedEvents) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout, batchTimespan);
    }

    /**
     *  Useful for batching events based on their received_at timestamp. Nakadi would flush a batch as soon as the
     *  difference in time between the first and the last event in the batch exceeds the batchTimespan.
     *
     * @param batchTimespan
     *   Nakadi's default (at the time of writing): 0, meaning "do not inspect timestamps".
     */
    public StreamParameters withBatchTimespan(int batchTimespan) {
        return new StreamParameters(batchLimit, streamLimit, batchFlushTimeout, streamTimeout, streamKeepAliveLimit, maxUncommittedEvents, commitTimeout, batchTimespan);
    }

    /**
     * @return
     * Maximum number of events in each chunk (and therefore per partition) of the stream.
     */
    public Optional<Integer> getBatchLimit() {
        return Optional.ofNullable(batchLimit);
    }

    /**
     * @return
     * Maximum number of events in this stream (over all partitions being streamed in this connection).
     */
    public Optional<Integer> getStreamLimit() {
        return Optional.ofNullable(streamLimit);
    }

    /**
     * @return
     * Maximum time in seconds to wait for the flushing of each chunk (per partition).
     */
    public Optional<Integer> getBatchFlushTimeout() {
        return Optional.ofNullable(batchFlushTimeout);
    }

    /**
     * @return
     * Maximum time in seconds a stream will live before connection is closed by the server.
     */
    public Optional<Integer> getStreamTimeout() {
        return Optional.ofNullable(streamTimeout);
    }

    /**
     * @return
     * Maximum number of empty keep alive batches to get in a row before closing the connection.
     */
    public Optional<Integer> getStreamKeepAliveLimit() {
        return Optional.ofNullable(streamKeepAliveLimit);
    }

    /**
     * @return
     * The maximum number of uncommitted events that Nakadi will stream before pausing the stream.
     */
    public Optional<Integer> getMaxUncommittedEvents() {
        return Optional.ofNullable(maxUncommittedEvents);
    }

    /**
     * @return
     * Maximum amount of seconds that Nakadi will be waiting for commit after sending a
     *                      batch to a client
     */
    public Optional<Integer> getCommitTimeout() {
        return Optional.ofNullable(commitTimeout);
    }

    /**
     * @return
     *  Nakadi would flush a batch as soon as the
     *  difference in time between the first and the last event in the batch exceeds the batchTimespan.
     */
    public Optional<Integer> getBatchTimespan() {
        return Optional.ofNullable(batchTimespan);
    }

}
