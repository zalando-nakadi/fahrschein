package org.zalando.fahrschein;

import java.io.IOException;

public class BackoffException extends Exception {
    private final int retries;

    public BackoffException(final IOException cause, final int retries) {
        super(cause);
        this.retries = retries;
    }

    public int getRetries() {
        return retries;
    }

    @Override
    public synchronized IOException getCause() {
        return (IOException)super.getCause();
    }
}
