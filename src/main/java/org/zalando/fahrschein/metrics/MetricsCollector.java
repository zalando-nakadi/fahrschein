package org.zalando.fahrschein.metrics;

public interface MetricsCollector {
    void markBatchesReceived();

    void markEventsReceived(int size);

    void markErrorWhileConsuming();

    void markReconnection();
}
