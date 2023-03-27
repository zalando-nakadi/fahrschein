package org.zalando.fahrschein.metrics.micrometer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.zalando.fahrschein.MetricsCollector;

class CountingMetricsCollector implements MetricsCollector {

    public static final String DEFAULT_PREFIX = "org.zalando.fahrschein";

    private final Counter messagesReceivedMeter;
    private final Counter eventsReceivedMeter;
    private final Counter errorsWhileConsumingMeter;
    private final Counter reconnectionsMeter;
    private final Counter messagesSuccessfullyProcessedMeter;

    public CountingMetricsCollector(final MeterRegistry metricRegistry) {
        this(metricRegistry, DEFAULT_PREFIX);
    }

    public CountingMetricsCollector(final MeterRegistry metricRegistry, final String prefix) {
        messagesReceivedMeter = metricRegistry.counter(prefix + "messagesReceived");
        eventsReceivedMeter = metricRegistry.counter(prefix + "eventsReceived");
        errorsWhileConsumingMeter = metricRegistry.counter(prefix + "errorsWhileConsuming");
        reconnectionsMeter = metricRegistry.counter(prefix + "reconnections");
        messagesSuccessfullyProcessedMeter = metricRegistry.counter(prefix + "messagesSuccessfullyProcessed");
    }

    @Override
    public void markMessageReceived() {
        messagesReceivedMeter.increment();
    }

    @Override
    public void markEventsReceived(final int size) {
        eventsReceivedMeter.increment(size);
    }

    @Override
    public void markErrorWhileConsuming() {
        errorsWhileConsumingMeter.increment();
    }

    @Override
    public void markReconnection() {
        reconnectionsMeter.increment();
    }

    @Override
    public void markMessageSuccessfullyProcessed() {
        messagesSuccessfullyProcessedMeter.increment();
    }
}
