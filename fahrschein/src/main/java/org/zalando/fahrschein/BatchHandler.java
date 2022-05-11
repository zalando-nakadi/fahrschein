package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Cursor;

import java.io.IOException;
import java.util.List;

/**
 * Allows wrapping {@link Listener#accept(List)} and {@link CursorManager#onSuccess(String, Cursor)} inside one transaction,
 * with automatic rollback if cursor commit fails.
 *
 * This is mainly useful when cursors are persisted in the same transactional datasource that the listener is using.
 *
 * Be careful when using this with a {@link ManagedCursorManager}, as it is possible that the response from a successful commit gets lost by the network.
 * When the actions of the {@link Listener} are now rolled back, those events won't be received again.
 */
public interface BatchHandler {
    /**
     * @param continuation A closure which will process the current batch when called.
     */
    void processBatch(IORunnable continuation) throws IOException;

}
