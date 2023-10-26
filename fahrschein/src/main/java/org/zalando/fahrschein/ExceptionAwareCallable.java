package org.zalando.fahrschein;

import java.io.IOException;
@FunctionalInterface
public interface ExceptionAwareCallable<T> {

    T call(int retryCount, EventPersistenceException exception) throws IOException, BackoffException, InterruptedException;
}
