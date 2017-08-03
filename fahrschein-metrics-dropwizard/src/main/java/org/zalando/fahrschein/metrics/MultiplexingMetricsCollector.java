package org.zalando.fahrschein.metrics;

import org.zalando.fahrschein.MetricsCollector;

import java.util.Collection;
import java.util.LinkedList;

public class MultiplexingMetricsCollector implements MetricsCollector {

    private final Collection<MetricsCollector> delegates = new LinkedList<>();

    @Override
    public void markMessageReceived() {
        for (MetricsCollector delegate : delegates) {
            delegate.markMessageReceived();
        }
    }

    @Override
    public void markEventsReceived(final int i) {
        for (MetricsCollector delegate : delegates) {
            delegate.markEventsReceived(i);
        }
    }

    @Override
    public void markErrorWhileConsuming() {
        for (MetricsCollector delegate : delegates) {
            delegate.markErrorWhileConsuming();
        }
    }

    @Override
    public void markReconnection() {
        for (MetricsCollector delegate : delegates) {
            delegate.markReconnection();
        }
    }

    @Override
    public void markMessageSuccessfullyProcessed() {
        for (MetricsCollector delegate : delegates) {
            delegate.markMessageSuccessfullyProcessed();
        }
    }

    public MultiplexingMetricsCollector register(final MetricsCollector metricsCollector) {
        delegates.add(metricsCollector);
        return this;
    }

}
