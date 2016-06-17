package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Cursor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryCursorManager implements CursorManager {
    private final Map<String, Map<String, Cursor>> partitionsByEventName = new HashMap<>();

    @Override
    public void onSuccess(String eventName, Cursor cursor) {
        partitionsByEventName.computeIfAbsent(eventName, key -> new HashMap<>()).put(cursor.getPartition(), cursor);
    }

    @Override
    public void onError(String eventName, Cursor cursor, EventProcessingException e) {
    }

    @Override
    public Collection<Cursor> getCursors(String eventName) {
        return Collections.unmodifiableCollection(partitionsByEventName.get(eventName).values());
    }
}
