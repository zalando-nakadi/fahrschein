package org.zalando.fahrschein;

import java.util.Random;

public class EqualJitterBackoffStrategy extends ExponentialBackoffStrategy {
    private final Random random = new Random();

    public EqualJitterBackoffStrategy() {
    }

    public EqualJitterBackoffStrategy(int initialDelay, double backoffFactor, long maxDelay, int maxRetries) {
        super(initialDelay, backoffFactor, maxDelay, maxRetries);
    }

    private EqualJitterBackoffStrategy(ExponentialBackoffStrategy other) {
        super(other);
    }

    public EqualJitterBackoffStrategy withMaxRetries(int maxRetries) {
        return new EqualJitterBackoffStrategy(super.withMaxRetries(maxRetries));
    }

    protected long calculateDelay(double count) {
        final long ceil = super.calculateDelay(count);
        return (ceil / 2) + (long)(random.nextDouble() * ceil / 2);
    }
}