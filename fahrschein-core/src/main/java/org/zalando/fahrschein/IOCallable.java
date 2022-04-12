package org.zalando.fahrschein;

import java.io.IOException;
import java.util.concurrent.Callable;

@FunctionalInterface
public interface IOCallable<T> extends Callable<T> {
    T call() throws IOException;
}
