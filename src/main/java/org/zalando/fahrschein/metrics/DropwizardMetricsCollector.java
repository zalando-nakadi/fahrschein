package org.zalando.fahrschein.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

public class DropwizardMetricsCollector implements MetricsCollector {

    public static final String DEFAULT_PREFIX = "org.zalando.fahrschein.";

    private Meter batchesReceivedMeter;
    private Meter eventsReceivedMeter;
    private Meter errorsWhileConsumingMeter;
    private Meter reconnectionsMeter;

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry) {
        this(metricRegistry, DEFAULT_PREFIX);
    }

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry, final String prefix) {
        batchesReceivedMeter = metricRegistry.meter(prefix + "batchesReceived");
        eventsReceivedMeter = metricRegistry.meter(prefix + "eventsReceived");
        errorsWhileConsumingMeter = metricRegistry.meter(prefix + "errorsWhileConsuming");
        reconnectionsMeter = metricRegistry.meter(prefix + "reconnections");
    }

    @Override
    public void markMessageReceived() {
        batchesReceivedMeter.mark();
    }

    @Override
    public void markEventsReceived(final int size) {
        eventsReceivedMeter.mark(size);
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
