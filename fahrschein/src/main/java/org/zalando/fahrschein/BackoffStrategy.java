package org.zalando.fahrschein;

import java.io.IOException;

public interface BackoffStrategy {
    <T> T call(int initialExceptionCount, IOException initialException, IOCallable<T> callable) throws BackoffException, InterruptedException;

    <T> T call(int retryCount, EventPersistenceException lastException, ExceptionAwareCallable<T> callable) throws BackoffException, InterruptedException;
}
