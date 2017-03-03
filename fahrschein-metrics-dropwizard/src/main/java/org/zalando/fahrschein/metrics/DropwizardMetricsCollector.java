package org.zalando.fahrschein.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.zalando.fahrschein.MetricsCollector;

import java.time.OffsetDateTime;
import java.util.Optional;

public class DropwizardMetricsCollector implements MetricsCollector {

    public static final String DEFAULT_PREFIX = "org.zalando.fahrschein.";

    private final Meter messagesReceivedMeter;
    private final Meter eventsReceivedMeter;
    private final Meter errorsWhileConsumingMeter;
    private final Meter reconnectionsMeter;
    private final Meter messagesSuccessfullyProcessedMeter;

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry) {
        this(metricRegistry, DEFAULT_PREFIX);
    }

    public DropwizardMetricsCollector(final MetricRegistry metricRegistry, final String prefix) {
        messagesReceivedMeter = metricRegistry.meter(prefix + "messagesReceived");
        eventsReceivedMeter = metricRegistry.meter(prefix + "eventsReceived");
        errorsWhileConsumingMeter = metricRegistry.meter(prefix + "errorsWhileConsuming");
        reconnectionsMeter = metricRegistry.meter(prefix + "reconnections");
        messagesSuccessfullyProcessedMeter = metricRegistry.meter(prefix + "messagesSuccessfullyProcessed");
    }

    @Override
    public void markMessageReceived() {
        messagesReceivedMeter.mark();
    }

    @Override
    public void markEventsReceived(final int size, final Optional<OffsetDateTime> oldestOccurredAt, final Optional<OffsetDateTime> latestOccurredAt) {
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
