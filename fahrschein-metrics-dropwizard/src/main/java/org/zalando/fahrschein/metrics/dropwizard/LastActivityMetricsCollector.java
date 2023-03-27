package org.zalando.fahrschein.metrics.dropwizard;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.zalando.fahrschein.MetricsCollector;

import java.util.function.LongSupplier;

import static com.codahale.metrics.MetricRegistry.name;
import static java.lang.System.currentTimeMillis;

class LastActivityMetricsCollector implements MetricsCollector {

    private long lastMessageReceived = 0;
    private long lastMessageSuccessfullyProcessed = 0;
    private long lastEventReceived = 0;
    private long lastErrorHappend = 0;
    private long lastReconnect = 0;

    public LastActivityMetricsCollector(final MetricRegistry metricRegistry, final String metricsNamePrefix) {
        createOrReplaceGauge(metricRegistry,
                name(metricsNamePrefix, "lastMessageReceived"),
                () -> ((currentTimeMillis() - lastMessageReceived) / 1000));
        createOrReplaceGauge(metricRegistry,
                name( metricsNamePrefix, "lastMessageSuccessfullyProcessed"),
                () -> ((currentTimeMillis() - lastMessageSuccessfullyProcessed) / 1000));
        createOrReplaceGauge(metricRegistry,
                name(metricsNamePrefix, "lastEventReceived"),
                () -> ((currentTimeMillis() - lastEventReceived) / 1000));
        createOrReplaceGauge(metricRegistry,
                name( metricsNamePrefix, "lastErrorHappened"),
                () -> ((currentTimeMillis() - lastErrorHappend) / 1000));
        createOrReplaceGauge(metricRegistry,
                name(metricsNamePrefix, "lastReconnect"),
                () -> ((currentTimeMillis() - lastReconnect) / 1000));
    }

    private void createOrReplaceGauge(final MetricRegistry metricRegistry, final String gaugeName, final LongSupplier gaugeValueSupplier) {
        metricRegistry.remove(gaugeName);
        metricRegistry.register(gaugeName, (Gauge<Integer>) () -> (int) gaugeValueSupplier.getAsLong());
    }

    @Override
    public void markMessageReceived() {
        lastMessageReceived = currentTimeMillis();
    }

    @Override
    public void markEventsReceived(final int size) {
        lastEventReceived = currentTimeMillis();
    }

    @Override
    public void markErrorWhileConsuming() {
        lastErrorHappend = currentTimeMillis();
    }

    @Override
    public void markReconnection() {
        lastReconnect = currentTimeMillis();
    }

    @Override
    public void markMessageSuccessfullyProcessed() {
        lastMessageSuccessfullyProcessed = currentTimeMillis();
    }
}
