package org.zalando.fahrschein.metrics.micrometer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.zalando.fahrschein.MetricsCollector;

import static org.zalando.fahrschein.metrics.micrometer.MicrometerMetricsCollector.name;

class CountingMetricsCollector implements MetricsCollector {

    private final Counter messagesReceivedMeter;
    private final Counter eventsReceivedMeter;
    private final Counter errorsWhileConsumingMeter;
    private final Counter reconnectionsMeter;
    private final Counter messagesSuccessfullyProcessedMeter;

    public CountingMetricsCollector(final MeterRegistry metricRegistry) {
        this(metricRegistry, MicrometerMetricsCollector.DEFAULT_PREFIX);
    }

    public CountingMetricsCollector(final MeterRegistry metricRegistry, final String prefix) {
        messagesReceivedMeter = metricRegistry.counter(name(prefix, "messages","received"));
        eventsReceivedMeter = metricRegistry.counter(name(prefix, "events","received"));
        errorsWhileConsumingMeter = metricRegistry.counter(name(prefix, "errors","while","consuming"));
        reconnectionsMeter = metricRegistry.counter(name(prefix, "reconnections"));
        messagesSuccessfullyProcessedMeter = metricRegistry.counter(name(prefix, "messages","successfully","processed"));
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
