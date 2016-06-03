package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Cursor;

import java.util.Collection;

public interface CursorManager {
    void onSuccess(Cursor cursor);
    void onError(Cursor cursor, EventProcessingException throwable);
    Collection<Cursor> getCursors();
}
