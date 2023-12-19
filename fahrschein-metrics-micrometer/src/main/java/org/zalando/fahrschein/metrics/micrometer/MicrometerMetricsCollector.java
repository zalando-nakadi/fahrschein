package org.zalando.fahrschein.metrics.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import org.zalando.fahrschein.MetricsCollector;
import org.zalando.fahrschein.MultiplexingMetricsCollector;

import java.util.Arrays;

public class MicrometerMetricsCollector implements MetricsCollector {

    public static final String DEFAULT_PREFIX = name("fahrschein","listener");

    private final MetricsCollector delegate;

    public MicrometerMetricsCollector(final MeterRegistry metricRegistry) {
        this(metricRegistry, DEFAULT_PREFIX);
    }

    public MicrometerMetricsCollector(final MeterRegistry metricRegistry, final String id) {
        this.delegate = new MultiplexingMetricsCollector(Arrays.asList(
                new CountingMetricsCollector(metricRegistry, name(DEFAULT_PREFIX, id)),
                new LastActivityMetricsCollector(metricRegistry, name(DEFAULT_PREFIX, id))
        ));
    }

    public MicrometerMetricsCollector(final MeterRegistry metricRegistry, final String id, final String prefix) {
        this.delegate = new MultiplexingMetricsCollector(Arrays.asList(
                new CountingMetricsCollector(metricRegistry, name(prefix, id)),
                new LastActivityMetricsCollector(metricRegistry, name(prefix, id))
        ));
    }

    @Override
    public void markMessageReceived() {
        this.delegate.markMessageReceived();
    }

    @Override
    public void markEventsReceived(int size) {
        this.delegate.markEventsReceived(size);
    }

    @Override
    public void markErrorWhileConsuming() {
        this.delegate.markErrorWhileConsuming();
    }

    @Override
    public void markReconnection() {
        this.delegate.markReconnection();
    }

    @Override
    public void markMessageSuccessfullyProcessed() {
        this.delegate.markMessageSuccessfullyProcessed();
    }

    static String name(String name, String... names) {
        final StringBuilder builder = new StringBuilder();
        append(builder, name);
        if (names != null) {
            for (String s : names) {
                append(builder, s);
            }
        }
        return builder.toString();
    }

    private static void append(StringBuilder builder, String part) {
        if (part != null && !part.isEmpty()) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(part);
        }
    }

}
