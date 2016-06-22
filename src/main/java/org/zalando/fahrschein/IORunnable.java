package org.zalando.fahrschein;

import java.io.IOException;

@FunctionalInterface
public interface IORunnable {
    void run() throws IOException;
}
