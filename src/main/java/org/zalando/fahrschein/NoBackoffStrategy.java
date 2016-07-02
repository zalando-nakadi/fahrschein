package org.zalando.fahrschein;

import java.io.IOException;

public class NoBackoffStrategy implements BackoffStrategy{
    @Override
    public <T> T call(int initialCount, IOException initialException, IOCallable<T> callable) throws BackoffException, InterruptedException {
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
