package org.zalando.fahrschein;

import java.util.Random;


public class FullJitterBackoffStrategy extends ExponentialBackoffStrategy {
    private final Random random = new Random();

    public FullJitterBackoffStrategy() {
    }

    public FullJitterBackoffStrategy(int initialDelay, double backoffFactor, long maxDelay, int maxRetries) {
        super(initialDelay, backoffFactor, maxDelay, maxRetries);
    }

    private FullJitterBackoffStrategy(ExponentialBackoffStrategy other) {
        super(other);
    }

    public FullJitterBackoffStrategy withMaxRetries(int maxRetries) {
        return new FullJitterBackoffStrategy(super.withMaxRetries(maxRetries));
    }

    protected long calculateDelay(double count) {
        final long ceil = super.calculateDelay(count);
        return (long)(random.nextDouble() * ceil);
    }
}