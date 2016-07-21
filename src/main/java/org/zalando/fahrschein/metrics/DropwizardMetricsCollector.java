package org.zalando.fahrschein.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

public class DropwizardMetricsCollector implements MetricsCollector {

    public static final String DEFAULT_PREFIX = "org.zalando.fahrschein.";

    private final Meter batchesReceivedMeter;
    private final Meter eventsReceivedMeter;
    private final Meter errorsWhileConsumingMeter;
    private final Meter reconnectionsMeter;
    private final Meter messagesSuccessfullyProcessedMeter;

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry) {
        this(metricRegistry, DEFAULT_PREFIX);
    }

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry, final String prefix) {
        batchesReceivedMeter = metricRegistry.meter(prefix + "batchesReceived");
        eventsReceivedMeter = metricRegistry.meter(prefix + "eventsReceived");
        errorsWhileConsumingMeter = metricRegistry.meter(prefix + "errorsWhileConsuming");
        reconnectionsMeter = metricRegistry.meter(prefix + "reconnections");
        messagesSuccessfullyProcessedMeter = metricRegistry.meter(prefix + "messagesSuccessfullyProcessed");
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

    @Override
    public void markMessageSuccessfullyProcessed() {
        messagesSuccessfullyProcessedMeter.mark();
    }
}
