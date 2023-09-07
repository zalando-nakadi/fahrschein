package org.zalando.fahrschein.metrics.dropwizard;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.zalando.fahrschein.MetricsCollector;

import static com.codahale.metrics.MetricRegistry.name;

class CountingMetricsCollector implements MetricsCollector {

    private final Meter messagesReceivedMeter;
    private final Meter eventsReceivedMeter;
    private final Meter errorsWhileConsumingMeter;
    private final Meter reconnectionsMeter;
    private final Meter messagesSuccessfullyProcessedMeter;

    public CountingMetricsCollector(final MetricRegistry metricRegistry, final String prefix) {
        messagesReceivedMeter = metricRegistry.meter(name(prefix, "messagesReceived"));
        eventsReceivedMeter = metricRegistry.meter(name(prefix, "eventsReceived"));
        errorsWhileConsumingMeter = metricRegistry.meter(name ( prefix, "errorsWhileConsuming"));
        reconnectionsMeter = metricRegistry.meter(name(prefix, "reconnections"));
        messagesSuccessfullyProcessedMeter = metricRegistry.meter(name(prefix, "messagesSuccessfullyProcessed"));
    }

    @Override
    public void markMessageReceived() {
        messagesReceivedMeter.mark();
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
