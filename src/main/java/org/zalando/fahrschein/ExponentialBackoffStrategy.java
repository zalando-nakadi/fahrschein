package org.zalando.fahrschein;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class ExponentialBackoffStrategy {
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
        this.initialDelay = initialDelay;
        this.backoffFactor = backoffFactor;
        this.maxDelay = maxDelay;
        this.maxRetries = maxRetries;
    }

    private long calculateDelay(double count) {
        return Math.min((long)(initialDelay*Math.pow(backoffFactor, count)), maxDelay);
    }


    private void sleepForRetries(int count) throws InterruptedException {
        final long delay = calculateDelay(count);
        LOG.info("Retry [{}], sleeping for [{}] milliseconds", count, delay);
        Thread.sleep(delay);
    }

    public <T> T call(Callable<T> callable) throws ExponentialBackoffException, InterruptedException {
        return call(0, callable);
    }

    public <T> T call(int initialCount, Callable<T> callable) throws ExponentialBackoffException, InterruptedException{
        int count = initialCount;

        if (count > 0) {
            sleepForRetries(count);
        }

        while (true) {
            try {
                LOG.trace("Try [{}]", count);
                return callable.call();
            } catch (Exception e) {
                LOG.warn("Got [{}]", e.getClass().getSimpleName(), e);
                count++;

                if (maxRetries >= 0 && count > maxRetries) {
                    LOG.info("Maximum number of retries exceeded");
                    throw new ExponentialBackoffException(e);
                }
                sleepForRetries(count);
            }
        }
    }
}
