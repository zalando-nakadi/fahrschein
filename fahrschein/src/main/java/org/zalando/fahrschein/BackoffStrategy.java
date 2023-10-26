package org.zalando.fahrschein;

import java.io.IOException;

public interface BackoffStrategy {
    <T> T call(int initialExceptionCount, IOException initialException, IOCallable<T> callable) throws BackoffException, InterruptedException;

    <T> T call(EventPersistenceException lastException, int retryCount, ExceptionAwareCallable<T> callable) throws BackoffException, InterruptedException;
}
