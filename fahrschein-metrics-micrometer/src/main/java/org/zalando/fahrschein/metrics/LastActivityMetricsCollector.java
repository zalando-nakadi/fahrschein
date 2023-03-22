package org.zalando.fahrschein.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.zalando.fahrschein.MetricsCollector;

import static java.lang.System.currentTimeMillis;

public class LastActivityMetricsCollector implements MetricsCollector {

    private long lastMessageReceived = 0;
    private long lastMessageSuccessfullyProcessed = 0;
    private long lastEventReceived = 0;
    private long lastErrorHappend = 0;
    private long lastReconnect = 0;

    public LastActivityMetricsCollector(final MeterRegistry metricRegistry, final String metricsNamePrefix) {
        Gauge.builder(metricsNamePrefix + ".lastMessageReceived",
                () -> (int) ((currentTimeMillis() - lastMessageReceived) / 1000)).baseUnit("s").register(metricRegistry);
        Gauge.builder(metricsNamePrefix + ".lastMessageSuccessfullyProcessed",
                () -> (int) ((currentTimeMillis() - lastMessageSuccessfullyProcessed) / 1000)).baseUnit("s").register(metricRegistry);
        Gauge.builder(metricsNamePrefix + ".lastEventReceived",
                () -> (int) ((currentTimeMillis() - lastEventReceived) / 1000)).baseUnit("s").register(metricRegistry);
        Gauge.builder(metricsNamePrefix + ".lastErrorHappened",
                () -> (int) ((currentTimeMillis() - lastErrorHappend) / 1000)).baseUnit("s").register(metricRegistry);
        Gauge.builder(metricsNamePrefix + ".lastReconnect",
                () -> (int) ((currentTimeMillis() - lastReconnect) / 1000)).baseUnit("s").register(metricRegistry);
    }

    @Override
    public void markMessageReceived() {
        lastMessageReceived = currentTimeMillis();
    }

    @Override
    public void markEventsReceived(final int size) {
        lastEventReceived = currentTimeMillis();
    }

    @Override
    public void markErrorWhileConsuming() {
        lastErrorHappend = currentTimeMillis();
    }

    @Override
    public void markReconnection() {
        lastReconnect = currentTimeMillis();
    }

    @Override
    public void markMessageSuccessfullyProcessed() {
        lastMessageSuccessfullyProcessed = currentTimeMillis();
    }
}
