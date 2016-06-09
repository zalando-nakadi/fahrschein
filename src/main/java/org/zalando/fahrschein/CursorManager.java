package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Partition;

import java.util.Collection;
import java.util.List;

public interface CursorManager {
    void onSuccess(Cursor cursor);
    void onError(Cursor cursor, EventProcessingException throwable);
    Collection<Cursor> getCursors();

    default void fromNewestAvailableOffsets(List<Partition> partitions) {
        for (Partition partition : partitions) {
            onSuccess(new Cursor(partition.getPartition(), partition.getNewestAvailableOffset()));
        }
    }

    default void fromOldestAvailableOffset(List<Partition> partitions) {
        for (Partition partition : partitions) {
            final String oldestAvailableOffset = partition.getOldestAvailableOffset();
            final String newestAvailableOffset = partition.getNewestAvailableOffset();
            final boolean isAvailable = oldestAvailableOffset.compareTo(newestAvailableOffset) <= 0;
            onSuccess(new Cursor(partition.getPartition(), isAvailable ? oldestAvailableOffset : newestAvailableOffset));
        }
    }

}
