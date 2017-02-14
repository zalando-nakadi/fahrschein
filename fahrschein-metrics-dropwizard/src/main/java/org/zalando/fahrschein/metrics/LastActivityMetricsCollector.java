package org.zalando.fahrschein.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.zalando.fahrschein.MetricsCollector;

import static com.codahale.metrics.MetricRegistry.name;
import static java.lang.System.currentTimeMillis;

public class LastActivityMetricsCollector implements MetricsCollector {

    private long lastMessageReceived = 0;
    private long lastMessageSuccessfullyProcessed = 0;
    private long lastEventReceived = 0;
    private long lastErrorHappend = 0;
    private long lastReconnect = 0;

    private final MetricRegistry metricRegistry;

    public LastActivityMetricsCollector(final MetricRegistry metricRegistry, final String metricsNamePrefix) {
        this.metricRegistry = metricRegistry;

        createOrReplaceGauge(metricRegistry,
                name(this.getClass(), metricsNamePrefix, "lastMessageReceived"),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return (int)((currentTimeMillis() - lastMessageReceived) / 1000);
                    }
                });
        createOrReplaceGauge(metricRegistry,
                name(this.getClass(), metricsNamePrefix, "lastMessageSuccessfullyProcessed"),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return (int)((currentTimeMillis() - lastMessageSuccessfullyProcessed) / 1000);
                    }
                });
        createOrReplaceGauge(metricRegistry,
                name(this.getClass(), metricsNamePrefix, "lastEventReceived"),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return (int)((currentTimeMillis() - lastEventReceived) / 1000);
                    }
                });
        createOrReplaceGauge(metricRegistry,
                name(this.getClass(), metricsNamePrefix, "lastErrorHappend"),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return (int)((currentTimeMillis() - lastErrorHappend) / 1000);
                    }
                });
        createOrReplaceGauge(metricRegistry,
                name(this.getClass(), metricsNamePrefix, "lastReconnect"),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return (int)((currentTimeMillis() - lastReconnect) / 1000);
                    }
                });
    }

    private void createOrReplaceGauge(final MetricRegistry metricRegistry, final String gaugeName, final Gauge<Integer> gauge) {
        metricRegistry.remove(gaugeName);
        metricRegistry.register(gaugeName, gauge);
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
