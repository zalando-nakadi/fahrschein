package org.zalando.fahrschein;

import javax.annotation.Nullable;
import java.io.IOException;

public interface BackoffStrategy {
    <T> T call(int initialExceptionCount, @Nullable IOException initialException, IOCallable<T> callable) throws BackoffException, InterruptedException;
}
