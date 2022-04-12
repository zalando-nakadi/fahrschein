package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Manages cursor offsets for one consumer. One consumer can handle several distinct events.
 */
public interface CursorManager {

    void onSuccess(String eventName, Cursor cursor) throws IOException;
    void onSuccess(String eventName, List<Cursor> cursors) throws IOException;

    Collection<Cursor> getCursors(String eventName) throws IOException;

    default void addSubscription(Subscription subscription) {

    }

    default void addStreamId(Subscription subscription, String streamId) {

    }

}
