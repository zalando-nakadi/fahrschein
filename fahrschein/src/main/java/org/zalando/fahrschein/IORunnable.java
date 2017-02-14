package org.zalando.fahrschein;

import java.io.IOException;

public interface IORunnable {
    class Wrapper implements Runnable {
        private final IORunnable delegate;

        public Wrapper(IORunnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try {
                delegate.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void run() throws IOException;

}
