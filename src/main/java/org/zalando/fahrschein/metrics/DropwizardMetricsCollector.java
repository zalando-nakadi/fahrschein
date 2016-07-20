package org.zalando.fahrschein.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

public class DropwizardMetricsCollector implements MetricsCollector {

    public static final String DEFAULT_PREFIX = "org.zalando.fahrschein.";

    private final MetricRegistry metricRegistry;
    private final String prefix;
    private Counter batchesReceivedCounter;
    private Histogram eventsReceivedHistogram;
    private Counter errorsWhileConsumingCounter;
    private Counter reconnectionsCounter;

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry) {
        this(metricRegistry, DEFAULT_PREFIX);
    }

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry, final String prefix) {
        this.metricRegistry = metricRegistry;
        this.prefix = prefix;

        batchesReceivedCounter = metricRegistry.counter(prefix + "batchesReceived");
        eventsReceivedHistogram = metricRegistry.histogram(prefix + "eventsReceived");
        errorsWhileConsumingCounter = metricRegistry.counter(prefix + "errorsWhileConsuming");
        reconnectionsCounter = metricRegistry.counter(prefix + "reconnections");
    }

    @Override
    public void markBatchesReceived() {
        batchesReceivedCounter.inc();
    }

    @Override
    public void markEventsReceived(final int size) {
        eventsReceivedHistogram.update(size);
    }

    @Override
    public void markErrorWhileConsuming() {
        errorsWhileConsumingCounter.inc();
    }

    @Override
    public void markReconnection() {
        reconnectionsCounter.inc();
    }
}
