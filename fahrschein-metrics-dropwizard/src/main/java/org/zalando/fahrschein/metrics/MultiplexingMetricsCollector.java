package org.zalando.fahrschein.metrics;

import org.zalando.fahrschein.MetricsCollector;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

public class MultiplexingMetricsCollector implements MetricsCollector {

    private final Collection<MetricsCollector> delegates = new LinkedList<>();

    @Override
    public void markMessageReceived() {
        delegates.stream().forEach(mc -> mc.markMessageReceived());
    }

    @Override
    public void markEventsReceived(final int i, final Optional<OffsetDateTime> oldestOccurredAt, final Optional<OffsetDateTime> latestOccurredAt) {
        delegates.stream().forEach(mc -> mc.markEventsReceived(i, oldestOccurredAt, latestOccurredAt));
    }

    @Override
    public void markErrorWhileConsuming() {
        delegates.stream().forEach(mc -> mc.markErrorWhileConsuming());
    }

    @Override
    public void markReconnection() {
        delegates.stream().forEach(mc -> mc.markReconnection());
    }

    @Override
    public void markMessageSuccessfullyProcessed() {
        delegates.stream().forEach(mc -> mc.markMessageSuccessfullyProcessed());
    }

    public MultiplexingMetricsCollector register(final MetricsCollector metricsCollector) {
        delegates.add(metricsCollector);
        return this;
    }

}
