package org.zalando.fahrschein.metrics.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import org.zalando.fahrschein.MetricsCollector;
import org.zalando.fahrschein.MultiplexingMetricsCollector;

import java.util.Arrays;

public class MicrometerMetricsCollector implements MetricsCollector {

    public static final String DEFAULT_PREFIX = "org.zalando.fahrschein.";

    private final MetricsCollector delegate;

    public MicrometerMetricsCollector(final MeterRegistry metricRegistry) {
        this(metricRegistry, DEFAULT_PREFIX);
    }

    public MicrometerMetricsCollector(final MeterRegistry metricRegistry, final String prefix) {
        this.delegate = new MultiplexingMetricsCollector(Arrays.asList(
                new CountingMetricsCollector(metricRegistry, prefix),
                new LastActivityMetricsCollector(metricRegistry, prefix)
        ));
    }

    @Override
    public void markMessageReceived() {
        this.delegate.markMessageReceived();
    }

    @Override
    public void markEventsReceived(int size) {
        this.delegate.markEventsReceived(size);
    }

    @Override
    public void markErrorWhileConsuming() {
        this.delegate.markErrorWhileConsuming();
    }

    @Override
    public void markReconnection() {
        this.markReconnection();
    }

    @Override
    public void markMessageSuccessfullyProcessed() {
        this.markMessageSuccessfullyProcessed();
    }
}
