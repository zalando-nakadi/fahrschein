package org.zalando.fahrschein;

import java.io.IOException;
@FunctionalInterface
public interface ExceptionAwareCallable<T> {

    T call(int retryCount, EnrichedEventPersistenceException exception) throws IOException, BackoffException, InterruptedException;
}
