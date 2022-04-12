package org.zalando.fahrschein;

import java.io.IOException;

class DefaultBatchHandler implements BatchHandler {
    public static final BatchHandler INSTANCE = new DefaultBatchHandler();

    private DefaultBatchHandler() {
    }

    @Override
    public void processBatch(IORunnable continuation) throws IOException {
        continuation.run();
    }
}
