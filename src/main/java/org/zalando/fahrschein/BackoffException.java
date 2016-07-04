package org.zalando.fahrschein;

import java.io.IOException;

public class BackoffException extends Exception {
    public BackoffException(IOException cause) {
        super(cause);
    }

    @Override
    public synchronized IOException getCause() {
        return (IOException)super.getCause();
    }
}
