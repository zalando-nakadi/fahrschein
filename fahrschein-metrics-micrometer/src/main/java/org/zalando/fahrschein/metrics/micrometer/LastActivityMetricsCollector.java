package org.zalando.fahrschein.metrics.micrometer;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.TimeGauge;
import org.zalando.fahrschein.MetricsCollector;

import java.util.concurrent.TimeUnit;

class LastActivityMetricsCollector implements MetricsCollector {

    private long lastMessageReceived = 0;
    private long lastMessageSuccessfullyProcessed = 0;
    private long lastEventReceived = 0;
    private long lastErrorHappend = 0;
    private long lastReconnect = 0;

    final Clock clock;

    LastActivityMetricsCollector(final MeterRegistry metricRegistry) {
        this(metricRegistry, CountingMetricsCollector.DEFAULT_PREFIX);
    }


    LastActivityMetricsCollector(final MeterRegistry metricRegistry, final String metricsNamePrefix) {
        clock = metricRegistry.config().clock();
        TimeGauge.builder(metricsNamePrefix + "lastMessageReceived",
                () -> (int) ((clock.wallTime() - lastMessageReceived) / 1000), TimeUnit.SECONDS).register(metricRegistry);
        TimeGauge.builder(metricsNamePrefix + "lastMessageSuccessfullyProcessed",
                () -> (int) ((clock.wallTime() - lastMessageSuccessfullyProcessed) / 1000), TimeUnit.SECONDS).register(metricRegistry);
        TimeGauge.builder(metricsNamePrefix + "lastEventReceived",
                () -> (int) ((clock.wallTime() - lastEventReceived) / 1000), TimeUnit.SECONDS).register(metricRegistry);
        TimeGauge.builder(metricsNamePrefix + "lastErrorHappened",
                () -> (int) ((clock.wallTime() - lastErrorHappend) / 1000), TimeUnit.SECONDS).register(metricRegistry);
        TimeGauge.builder(metricsNamePrefix + "lastReconnect",
                () -> (int) ((clock.wallTime() - lastReconnect) / 1000), TimeUnit.SECONDS).register(metricRegistry);
    }

    @Override
    public void markMessageReceived() {
        lastMessageReceived = clock.wallTime();
    }

    @Override
    public void markEventsReceived(final int size) {
        lastEventReceived = clock.wallTime();
    }

    @Override
    public void markErrorWhileConsuming() {
        lastErrorHappend = clock.wallTime();
    }

    @Override
    public void markReconnection() {
        lastReconnect = clock.wallTime();
    }

    @Override
    public void markMessageSuccessfullyProcessed() {
        lastMessageSuccessfullyProcessed = clock.wallTime();
    }
}
