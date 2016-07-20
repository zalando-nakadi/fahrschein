package org.zalando.fahrschein.metrics;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

public class DropwizardMetricsCollector implements MetricsCollector {

    public static final String DEFAULT_PREFIX = "org.zalando.fahrschein.";

    private final MetricRegistry metricRegistry;
    private final String prefix;
    private Meter batchesReceivedMeter;
    private Histogram eventsReceivedHistogram;
    private Meter errorsWhileConsumingMeter;
    private Meter reconnectionsMeter;

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry) {
        this(metricRegistry, DEFAULT_PREFIX);
    }

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry, final String prefix) {
        this.metricRegistry = metricRegistry;
        this.prefix = prefix;

        batchesReceivedMeter = metricRegistry.meter(prefix + "batchesReceived");
        eventsReceivedHistogram = metricRegistry.histogram(prefix + "eventsReceived");
        errorsWhileConsumingMeter = metricRegistry.meter(prefix + "errorsWhileConsuming");
        reconnectionsMeter = metricRegistry.meter(prefix + "reconnections");
    }

    @Override
    public void markBatchesReceived() {
        batchesReceivedMeter.mark();
    }

    @Override
    public void markEventsReceived(final int size) {
        eventsReceivedHistogram.update(size);
    }

    @Override
    public void markErrorWhileConsuming() {
        errorsWhileConsumingMeter.mark();
    }

    @Override
    public void markReconnection() {
        reconnectionsMeter.mark();
    }
}
