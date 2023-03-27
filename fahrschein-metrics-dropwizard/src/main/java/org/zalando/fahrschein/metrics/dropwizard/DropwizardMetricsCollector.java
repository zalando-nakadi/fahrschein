package org.zalando.fahrschein.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import org.zalando.fahrschein.MetricsCollector;
import org.zalando.fahrschein.MultiplexingMetricsCollector;

import java.util.Arrays;

public class DropwizardMetricsCollector implements MetricsCollector {

    public static final String DEFAULT_PREFIX = "org.zalando.fahrschein";

    private final MetricsCollector delegate;

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry) {
       this(metricRegistry, DEFAULT_PREFIX);
    }

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry, String prefix) {
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
