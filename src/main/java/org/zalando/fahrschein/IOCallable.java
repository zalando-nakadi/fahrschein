package org.zalando.fahrschein;

import java.io.IOException;

@FunctionalInterface
public interface IOCallable<T> {
    T call() throws IOException;
}
