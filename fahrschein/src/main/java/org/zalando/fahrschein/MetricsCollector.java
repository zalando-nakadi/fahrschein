package org.zalando.fahrschein;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface MetricsCollector {
    void markMessageReceived();

    void markEventsReceived(int size, Optional<OffsetDateTime> oldestOccurredAt, Optional<OffsetDateTime> latestOccurredAt);

    void markErrorWhileConsuming();

    void markReconnection();

    void markMessageSuccessfullyProcessed();
}
