package org.zalando.fahrschein;

import java.io.IOException;

public interface BatchHandler {
    /**
     * @param runnable A closure which will process the current batch when called.
     */
    void processBatch(IORunnable runnable) throws IOException;

}
