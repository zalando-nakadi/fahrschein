package org.zalando.fahrschein;

import java.io.IOException;

/**
 * Created by jh on 02.07.16.
 */
public interface BackoffStrategy {
    <T> T call(int initialCount, IOException initialException, IOCallable<T> callable) throws BackoffException, InterruptedException;
}
