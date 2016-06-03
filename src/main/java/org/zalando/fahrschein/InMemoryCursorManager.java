package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Cursor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryCursorManager implements CursorManager {
    private final Map<String, Cursor> cursorByPartition = new HashMap<>();

    @Override
    public void onSuccess(Cursor cursor) {
        cursorByPartition.put(cursor.getPartition(), cursor);
    }

    @Override
    public void onError(Cursor cursor, EventProcessingException e) {
    }

    @Override
    public Collection<Cursor> getCursors() {
        return Collections.unmodifiableCollection(cursorByPartition.values());
    }
}
