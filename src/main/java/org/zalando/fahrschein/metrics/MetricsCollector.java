package org.zalando.fahrschein.metrics;

public interface MetricsCollector {
    void markMessageReceived();

    void markEventsReceived(int size);

    void markErrorWhileConsuming();

    void markReconnection();

    void markMessageSuccessfullyProcessed();
}
