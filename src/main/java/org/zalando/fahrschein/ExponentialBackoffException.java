package org.zalando.fahrschein;

public class ExponentialBackoffException extends Exception {
    public ExponentialBackoffException(Throwable cause) {
        super(cause);
    }
}
