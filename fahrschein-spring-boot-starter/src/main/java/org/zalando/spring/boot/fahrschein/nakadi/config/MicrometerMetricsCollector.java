package org.zalando.spring.boot.fahrschein.nakadi.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.util.Assert;
import org.zalando.fahrschein.MetricsCollector;

public class MicrometerMetricsCollector implements MetricsCollector {

    private static final String MESSAGES_SUCCESSFULLY_PROCESSED = ".messages.successfully.processed";
    private static final String RECONNECTIONS = ".reconnections";
    private static final String ERRORS_WHILE_CONSUMING = ".errors.while.consuming";
    private static final String EVENTS_RECEIVED = ".events.received";
    private static final String MESSAGES_RECEIVED = ".messages.received";
    private static final String PREFIX = "fahrschein.listener.";

    private final Counter messagesReceivedMeter;
    private final Counter eventsReceivedMeter;
    private final Counter errorsWhileConsumingMeter;
    private final Counter reconnectionsMeter;
    private final Counter messagesSuccessfullyProcessedMeter;

    public MicrometerMetricsCollector(final MeterRegistry meterRegistry, final String id) {
        Assert.notNull(meterRegistry, "'meterRegistry' should never be null");
        Assert.hasText(id, "'id' should never be null or empty");
        messagesReceivedMeter = meterRegistry.counter(PREFIX + id + MESSAGES_RECEIVED);
        eventsReceivedMeter = meterRegistry.counter(PREFIX + id + EVENTS_RECEIVED);
        errorsWhileConsumingMeter = meterRegistry.counter(PREFIX + id + ERRORS_WHILE_CONSUMING);
        reconnectionsMeter = meterRegistry.counter(PREFIX + id + RECONNECTIONS);
        messagesSuccessfullyProcessedMeter = meterRegistry.counter(PREFIX + id + MESSAGES_SUCCESSFULLY_PROCESSED);
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
