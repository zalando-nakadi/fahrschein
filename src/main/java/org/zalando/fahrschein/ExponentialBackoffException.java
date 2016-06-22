package org.zalando.fahrschein;

import java.io.IOException;

public class ExponentialBackoffException extends Exception {
    public ExponentialBackoffException(IOException cause) {
        super(cause);
    }

    @Override
    public synchronized IOException getCause() {
        return (IOException)super.getCause();
    }
}
