package org.zalando.fahrschein;

import java.io.IOException;
import java.io.UncheckedIOException;

@FunctionalInterface
public interface IORunnable {
    void run() throws IOException;

    default Runnable unchecked() {
        return () -> {
            try {
                run();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

}
