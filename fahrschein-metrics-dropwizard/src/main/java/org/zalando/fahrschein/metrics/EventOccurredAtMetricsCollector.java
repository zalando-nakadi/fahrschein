package org.zalando.fahrschein.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Supplier;

public class EventOccurredAtMetricsCollector extends MetricsCollectorAdapter {

    private OffsetDateTime oldestOccurredAt;
    private OffsetDateTime latestOccurredAt;

    public EventOccurredAtMetricsCollector(final MetricRegistry metricRegistry, final String metricsNamePrefix) {
        createOrReplaceGauge(metricRegistry, metricsNamePrefix + "oldestOccurredAt", () -> this.oldestOccurredAt);
        createOrReplaceGauge(metricRegistry, metricsNamePrefix + "latestOccurredAt", () -> this.latestOccurredAt);
    }

    @Override
    public void markEventsReceived(final int size, final Optional<OffsetDateTime> oldestOccurredAt, final Optional<OffsetDateTime> latestOccurredAt) {
        oldestOccurredAt.ifPresent(value -> this.oldestOccurredAt = value);
        latestOccurredAt.ifPresent(value -> this.latestOccurredAt = value);
    }

    private static void createOrReplaceGauge(final MetricRegistry metricRegistry, final String gaugeName, final Supplier<OffsetDateTime> gaugeValueSupplier) {
        metricRegistry.remove(gaugeName);
        metricRegistry.register(gaugeName, (Gauge<OffsetDateTime>) gaugeValueSupplier::get);
    }

}
