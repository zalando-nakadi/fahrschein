package org.zalando.fahrschein.inmemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCursorManager implements CursorManager {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryCursorManager.class);

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Cursor>> partitionsByEventName = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Cursor> cursorsByPartition(final String eventName) {
        final ConcurrentHashMap<String, Cursor> next = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, Cursor> prev = partitionsByEventName.putIfAbsent(eventName, next);
        return prev != null ? prev : next;
    }

    @Override
    public void addSubscription(Subscription subscription) {

    }

    @Override
    public void addStreamId(Subscription subscription, String streamId) {

    }

    @Override
    public void onSuccess(final String eventName, final Cursor cursor) {
        cursorsByPartition(eventName).put(cursor.getPartition(), cursor);
    }

    @Override
    public void onSuccess(String eventName, List<Cursor> cursors) throws IOException {
        for (Cursor cursor : cursors) {
            onSuccess(eventName, cursor);
        }
    }

    @Override
    public Collection<Cursor> getCursors(final String eventName) {
        return Collections.unmodifiableCollection(cursorsByPartition(eventName).values());
    }
}
