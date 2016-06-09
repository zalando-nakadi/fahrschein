package org.zalando.fahrschein;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class ExponentialBackoffInputStreamSupplier implements InputStreamSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(ExponentialBackoffInputStreamSupplier.class);

    private final InputStreamSupplier delegate;
    private final ExponentialBackoffStrategy exponentialBackoffStrategy;

    public ExponentialBackoffInputStreamSupplier(final InputStreamSupplier delegate, final ExponentialBackoffStrategy exponentialBackoffStrategy) {
        this.delegate = delegate;
        this.exponentialBackoffStrategy = exponentialBackoffStrategy;
    }

    public InputStream open(final ConnectionParameters connectionParameters) throws IOException, InterruptedException {
        try {
            final int errorCount = connectionParameters.getErrorCount();
            LOG.trace("Current error count is [{}]", errorCount);
            return exponentialBackoffStrategy.call(errorCount, () -> delegate.open(connectionParameters));
        } catch (ExponentialBackoffException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            } else {
                throw new IllegalStateException(cause);
            }
        }
    }

}
