package org.zalando.fahrschein.metrics;

public class NoMetricsCollector implements MetricsCollector {
    @Override
    public void markMessageReceived() {
        // do nothing
    }

    @Override
    public void markEventsReceived(final int size) {
        // do nothing
    }

    @Override
    public void markErrorWhileConsuming() {
        // do nothing
    }

    @Override
    public void markReconnection() {
        // do nothing
    }

}
