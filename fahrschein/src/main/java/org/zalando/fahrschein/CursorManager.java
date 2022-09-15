package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Manages cursor offsets for one consumer. One consumer can handle several distinct event types.
 */
public interface CursorManager {

    /**
     * Commits one cursor on successful event consumption, i.e. if
     * {@link Listener#accept(List)}} did not throw an exception.
     * @param eventName event name
     * @param cursor    cursor to commit
     * @throws IOException in case of errors during committing
     */
    void onSuccess(String eventName, Cursor cursor) throws IOException;

    /**
     * Commits a list of cursors on successful event consumption for multiple cursors
     * of the same event type. This is useful to reset a consumer to a point in time
     * like the beginning or the end of an event type.
     *
     * @param eventName event name
     * @param cursors    cursors to commit
     * @throws IOException in case of errors during committing
     */
    void onSuccess(String eventName, List<Cursor> cursors) throws IOException;

    Collection<Cursor> getCursors(String eventName) throws IOException;

    default void addSubscription(Subscription subscription) {

    }

    default void addStreamId(Subscription subscription, String streamId) {

    }

}
