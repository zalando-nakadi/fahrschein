package org.zalando.fahrschein;

import javax.annotation.Nullable;
import java.io.IOException;

public class NoBackoffStrategy implements BackoffStrategy{
    @Override
    public <T> T call(final int initialExceptionCount, final @Nullable IOException initialException, final IOCallable<T> callable) throws BackoffException, InterruptedException {
        if (initialException != null) {
            throw new BackoffException(initialException);
        } else {
            try {
                return callable.call();
            } catch (IOException e) {
                throw new BackoffException(e);
            }
        }
    }
}
