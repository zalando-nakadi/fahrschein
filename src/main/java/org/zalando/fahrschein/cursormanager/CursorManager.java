package org.zalando.fahrschein.cursormanager;

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
    void onSuccess(String eventName, Cursor cursor) throws IOException;
    void onError(String eventName, Cursor cursor, Throwable throwable) throws IOException;
    Collection<Cursor> getCursors(String eventName) throws IOException;

    default void addSubscription(Subscription subscription) {

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
     * Initializes offsets to start streaming at the oldes available offset (BEGIN).
     */
    default void fromOldestAvailableOffset(String eventName, List<Partition> partitions) throws IOException {
        for (Partition partition : partitions) {
            onSuccess(eventName, new Cursor(partition.getPartition(), "BEGIN"));
        }
    }

    default void updatePartitions(String eventName, List<Partition> partitions) throws IOException {
        final Map<String, Cursor> cursorsByPartition = getCursors(eventName).stream().collect(Collectors.toMap(Cursor::getPartition, c -> c));

        for (Partition partition : partitions) {
            final Cursor cursor = cursorsByPartition.get(partition.getPartition());
            if (cursor == null || !partition.isAvailable(cursor.getOffset())) {
                onSuccess(eventName, new Cursor(partition.getPartition(), "BEGIN"));
            }
        }
    }

}
