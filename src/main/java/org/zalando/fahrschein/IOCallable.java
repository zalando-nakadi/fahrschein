package org.zalando.fahrschein;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Callable;

@FunctionalInterface
public interface IOCallable<T> {
    T call() throws IOException;

    default Callable<T> unchecked() {
        return () -> {
            try {
                return call();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
