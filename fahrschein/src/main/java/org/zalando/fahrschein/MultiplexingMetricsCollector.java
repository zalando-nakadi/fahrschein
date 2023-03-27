package org.zalando.fahrschein;

import java.util.List;

public class MultiplexingMetricsCollector implements MetricsCollector {

    private final List<MetricsCollector> delegates;


    public MultiplexingMetricsCollector(List<MetricsCollector> delegates) {
        this.delegates = delegates;
    }

    @Override
    public void markMessageReceived() {
        delegates.stream().forEach(mc -> mc.markMessageReceived());
    }

    @Override
    public void markEventsReceived(final int i) {
        delegates.stream().forEach(mc -> mc.markEventsReceived(i));
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

}
