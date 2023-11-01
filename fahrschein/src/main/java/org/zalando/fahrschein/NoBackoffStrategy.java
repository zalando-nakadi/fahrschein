package org.zalando.fahrschein;

import java.io.IOException;

public class NoBackoffStrategy implements BackoffStrategy {
    @Override
    public <T> T call(final int initialExceptionCount, final IOException initialException, final IOCallable<T> callable) throws BackoffException {
        throw new BackoffException(initialException, initialExceptionCount);
    }

    @Override
    public <T> T call(final int initialExceptionCount, final EventPersistenceException initialException,
            final ExceptionAwareCallable<T> callable) throws BackoffException {
        throw new BackoffException(initialException, initialExceptionCount);
    }
}
