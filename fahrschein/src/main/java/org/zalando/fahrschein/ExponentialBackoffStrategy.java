package org.zalando.fahrschein;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.zalando.fahrschein.Preconditions.checkState;


public class ExponentialBackoffStrategy implements BackoffStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(ExponentialBackoffStrategy.class);

    public static final int DEFAULT_INITIAL_DELAY = 500;
    public static final double DEFAULT_BACKOFF_FACTOR = 1.5;
    public static final long DEFAULT_MAX_DELAY = 10 * 60 * 1000L;

    private final int initialDelay;
    private final double backoffFactor;
    private final long maxDelay;
    private final int maxRetries;

    public ExponentialBackoffStrategy() {
        this(DEFAULT_INITIAL_DELAY, DEFAULT_BACKOFF_FACTOR, DEFAULT_MAX_DELAY, -1);
    }

    public ExponentialBackoffStrategy(int initialDelay, double backoffFactor, long maxDelay, int maxRetries) {
        checkState(initialDelay > 0, "Initial delay should be bigger than 0");

        this.initialDelay = initialDelay;
        this.backoffFactor = backoffFactor;
        this.maxDelay = maxDelay;
        this.maxRetries = maxRetries;
    }

    protected ExponentialBackoffStrategy(ExponentialBackoffStrategy other) {
        this(other.initialDelay, other.backoffFactor, other.maxDelay, other.maxRetries);
    }

    public ExponentialBackoffStrategy withMaxRetries(int maxRetries) {
        return new ExponentialBackoffStrategy(initialDelay, backoffFactor, maxDelay, maxRetries);
    }

    protected long calculateDelay(double count) {
        return Math.min((long) (initialDelay * Math.pow(backoffFactor, count)), maxDelay);
    }

    private void sleepForRetries(final int count) throws InterruptedException {
        final long delay = calculateDelay(count);
        LOG.info("Retry [{}], sleeping for [{}] milliseconds", count, delay);
        Thread.sleep(delay);
    }

    private void checkMaxRetries(final IOException exception, final int count) throws BackoffException {
        if (maxRetries >= 0 && count >= maxRetries) {
            LOG.info("Number of retries [{}] is higher than configured maximum [{}]", count, maxRetries);
            throw new BackoffException(exception, count);
        }
    }

    @Override
    public <T> T call(final int initialExceptionCount, final IOException initialException,
            final IOCallable<T> callable) throws BackoffException, InterruptedException {
        return performRetry(initialExceptionCount, initialException, callable);
    }

    @Override
    public <T> T call(final int initialExceptionCount, final EventPersistenceException initialException,
            final ExceptionAwareCallable<T> callable) throws BackoffException, InterruptedException {
        return performRetry(initialExceptionCount, initialException, callable);
    }

    private <T> T performRetry(final int initialExceptionCount, final IOException initialException,
            final Object callable) throws BackoffException, InterruptedException {
        checkMaxRetries(initialException, initialExceptionCount);
        int count = initialExceptionCount;
        if (count > 0) {
            sleepForRetries(count);
        }
        IOException lastRetryException = initialException;
        while (true) {
            try {
                LOG.trace("Try [{}]", count);
                if (callable instanceof ExceptionAwareCallable<?>) {
                    return ((ExceptionAwareCallable<T>) callable).call(count,
                            (EventPersistenceException) lastRetryException);
                }
                return ((IOCallable<T>) callable).call();
            } catch (EventPersistenceException e) {
                lastRetryException = e;
            } catch (IOException e) {
                lastRetryException = e;
                if (callable instanceof ExceptionAwareCallable<?>) {
                    throw new BackoffException(e, count);
                }
            } finally {
                LOG.warn("Got [{}] on retry [{}]", lastRetryException.getClass()
                        .getSimpleName(), count, lastRetryException);
                count++;
                checkMaxRetries(lastRetryException, count);
                sleepForRetries(count);
            }
        }
    }

}
