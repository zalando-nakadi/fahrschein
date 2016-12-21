package org.zalando.fahrschein;

import java.time.OffsetDateTime;
import java.util.Optional;

public class NoMetricsCollector implements MetricsCollector {

    public static final NoMetricsCollector NO_METRICS_COLLECTOR = new NoMetricsCollector();

    @Override
    public void markMessageReceived() {
        // do nothing
    }

    @Override
    public void markEventsReceived(final int size, final Optional<OffsetDateTime> oldestOccurredAt, final Optional<OffsetDateTime> latestOccurredAt) {
        // do nothing
    }

    @Override
    public void markErrorWhileConsuming() {
        // do nothing
    }

    @Override
    public void markReconnection() {
        // do nothing
    }

    @Override
    public void markMessageSuccessfullyProcessed() {
        // do nothing
    }

}
