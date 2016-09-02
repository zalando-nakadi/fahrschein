package org.zalando.fahrschein;

import com.google.common.collect.Ordering;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages cursor offsets for one consumer. One consumer can handle several distinct events.
 */
public interface CursorManager {

    Ordering<String> OFFSET_ORDERING = Ordering.natural().nullsFirst().onResultOf((String offset) -> "BEGIN".equals(offset) ? null : Long.parseLong(offset));

    void onSuccess(String eventName, Cursor cursor) throws IOException;

    void onError(String eventName, Cursor cursor, Throwable throwable) throws IOException;

    Collection<Cursor> getCursors(String eventName) throws IOException;

    default void addSubscription(Subscription subscription) {

    }

    default void addStreamId(Subscription subscription, String streamId) {

    }

    /**
     * Initializes offsets to start streaming from the newest available offset.
     */
    default void fromNewestAvailableOffsets(String eventName, List<Partition> partitions) throws IOException {
        for (Partition partition : partitions) {
            onSuccess(eventName, new Cursor(partition.getPartition(), partition.getNewestAvailableOffset()));
        }
    }

    /**
     * Initializes offsets to start streaming at the oldest available offset (BEGIN).
     */
    default void fromOldestAvailableOffset(String eventName, List<Partition> partitions) throws IOException {
        for (Partition partition : partitions) {
            onSuccess(eventName, new Cursor(partition.getPartition(), "BEGIN"));
        }
    }

    /**
     * Updates offsets in case the currently stored offset is no longer available. Streaming will start at the oldest available offset (BEGIN) to minimize the amount of events skipped.
     */
    default void updatePartitions(String eventName, List<Partition> partitions) throws IOException {

        final Map<String, Cursor> cursorsByPartition = getCursors(eventName).stream().collect(Collectors.toMap(Cursor::getPartition, c -> c));

        for (Partition partition : partitions) {
            final Cursor cursor = cursorsByPartition.get(partition.getPartition());
            if (cursor == null || (!"BEGIN".equals(cursor.getOffset()) && OFFSET_ORDERING.compare(cursor.getOffset(), partition.getOldestAvailableOffset()) < 0)) {
                onSuccess(eventName, new Cursor(partition.getPartition(), "BEGIN"));
            }
        }
    }

}
