package org.zalando.fahrschein.metrics.micrometer;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.TimeGauge;
import org.zalando.fahrschein.MetricsCollector;

import java.util.concurrent.TimeUnit;

import static org.zalando.fahrschein.metrics.micrometer.MicrometerMetricsCollector.name;

class LastActivityMetricsCollector implements MetricsCollector {

    private long lastMessageReceived = 0;
    private long lastMessageSuccessfullyProcessed = 0;
    private long lastEventReceived = 0;
    private long lastErrorHappened = 0;
    private long lastReconnect = 0;

    final Clock clock;

    LastActivityMetricsCollector(final MeterRegistry metricRegistry) {
        this(metricRegistry, MicrometerMetricsCollector.DEFAULT_PREFIX);
    }


    LastActivityMetricsCollector(final MeterRegistry metricRegistry, final String metricsNamePrefix) {
        clock = metricRegistry.config().clock();
        TimeGauge.builder(name(metricsNamePrefix, "last","message","received"),
                () -> (int) ((clock.wallTime() - lastMessageReceived) / 1000), TimeUnit.SECONDS).register(metricRegistry);
        TimeGauge.builder(name(metricsNamePrefix, "last","message","successfully","processed"),
                () -> (int) ((clock.wallTime() - lastMessageSuccessfullyProcessed) / 1000), TimeUnit.SECONDS).register(metricRegistry);
        TimeGauge.builder(name(metricsNamePrefix, "last","event","received"),
                () -> (int) ((clock.wallTime() - lastEventReceived) / 1000), TimeUnit.SECONDS).register(metricRegistry);
        TimeGauge.builder(name(metricsNamePrefix, "last","error","happened"),
                () -> (int) ((clock.wallTime() - lastErrorHappened) / 1000), TimeUnit.SECONDS).register(metricRegistry);
        TimeGauge.builder(name(metricsNamePrefix, "last","reconnect"),
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
        lastErrorHappened = clock.wallTime();
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
