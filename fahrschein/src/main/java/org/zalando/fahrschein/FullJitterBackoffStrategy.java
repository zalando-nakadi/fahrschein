package org.zalando.fahrschein;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


public class FullJitterBackoffStrategy extends ExponentialBackoffStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(FullJitterBackoffStrategy.class);
    private final Random random = new Random();

    public FullJitterBackoffStrategy() {
    }

    public FullJitterBackoffStrategy(int initialDelay, double backoffFactor, long maxDelay, int maxRetries) {
        super(initialDelay, backoffFactor, maxDelay, maxRetries);
    }

    private FullJitterBackoffStrategy(FullJitterBackoffStrategy other) {
        super(other);
    }

    public FullJitterBackoffStrategy withMaxRetries(int maxRetries) {
        return new FullJitterBackoffStrategy(this.withMaxRetries(maxRetries));
    }

    protected long calculateDelay(double count) {
        final long ceil = super.calculateDelay(count);
        return (long)(random.nextDouble() * ceil);
    }
}