package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Partition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface CursorManager {
    void onSuccess(String eventName, Cursor cursor);
    void onError(String eventName, Cursor cursor, EventProcessingException throwable);
    Collection<Cursor> getCursors(String eventName);

    default void fromNewestAvailableOffsets(String eventName, List<Partition> partitions) {
        for (Partition partition : partitions) {
            onSuccess(eventName, new Cursor(partition.getPartition(), partition.getNewestAvailableOffset()));
        }
    }

    default void fromOldestAvailableOffset(String eventName, List<Partition> partitions) {
        for (Partition partition : partitions) {
            onSuccess(eventName, new Cursor(partition.getPartition(), "BEGIN"));
        }
    }

    default void updatePartitions(String eventName, List<Partition> partitions) {
        final Map<String, Cursor> cursorsByPartition = getCursors(eventName).stream().collect(Collectors.toMap(Cursor::getPartition, c -> c));

        for (Partition partition : partitions) {
            final Cursor cursor = cursorsByPartition.get(partition.getPartition());
            if (cursor == null || !partition.isAvailable(cursor.getOffset())) {
                onSuccess(eventName, new Cursor(partition.getPartition(), "BEGIN"));
            }
        }
    }

}
