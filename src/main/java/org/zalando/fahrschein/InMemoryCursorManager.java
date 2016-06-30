package org.zalando.fahrschein;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.domain.Cursor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryCursorManager implements CursorManager {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryCursorManager.class);

    private final Map<String, Map<String, Cursor>> partitionsByEventName = new HashMap<>();

    @Override
    public void onSuccess(String eventName, Cursor cursor) {
        partitionsByEventName.computeIfAbsent(eventName, key -> new HashMap<>()).put(cursor.getPartition(), cursor);
    }

    @Override
    public void onError(String eventName, Cursor cursor, Throwable throwable) {
        LOG.warn("Exception while processing events for [{}] on partition [{}] at offset [{}]", eventName, cursor.getPartition(), cursor.getOffset(), throwable);
    }

    @Override
    public Collection<Cursor> getCursors(String eventName) {
        return partitionsByEventName.getOrDefault(eventName, Collections.emptyMap()).values();
    }
}
