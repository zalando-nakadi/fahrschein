package org.zalando.fahrschein.metrics;

import org.zalando.fahrschein.MetricsCollector;

import java.time.OffsetDateTime;
import java.util.Optional;

public abstract class MetricsCollectorAdapter implements MetricsCollector {

    @Override
    public void markMessageReceived() {

    }

    @Override
    public void markEventsReceived(final int size, final  Optional<OffsetDateTime> oldestOccurredAt, final  Optional<OffsetDateTime> latestOccurredAt) {

    }

    @Override
    public void markErrorWhileConsuming() {

    }

    @Override
    public void markReconnection() {

    }

    @Override
    public void markMessageSuccessfullyProcessed() {

    }
}
