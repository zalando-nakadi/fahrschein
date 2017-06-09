package org.zalando.fahrschein.jdbc;

import org.springframework.transaction.annotation.Transactional;
import org.zalando.fahrschein.IORunnable;
import org.zalando.fahrschein.BatchHandler;

import java.io.IOException;

public class TransactionalBatchHandler implements BatchHandler {

    @Override
    @Transactional(rollbackFor = {Error.class, RuntimeException.class, IOException.class})
    public void processBatch(final IORunnable runnable) throws IOException {
        runnable.run();
    }
}
