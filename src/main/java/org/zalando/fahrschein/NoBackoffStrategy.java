package org.zalando.fahrschein;

import javax.annotation.Nullable;
import java.io.IOException;

public class NoBackoffStrategy implements BackoffStrategy{
    @Override
    public <T> T call(final int initialExceptionCount, final @Nullable IOException initialException, final IOCallable<T> callable) throws BackoffException, InterruptedException {
        throw new BackoffException(initialException, initialExceptionCount);
    }
}
