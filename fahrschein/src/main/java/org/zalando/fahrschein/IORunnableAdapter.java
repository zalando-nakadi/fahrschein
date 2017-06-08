package org.zalando.fahrschein;

import java.io.IOException;

final class IORunnableAdapter implements Runnable {
    private final IORunnable delegate;

    public IORunnableAdapter(IORunnable delegate) {
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
