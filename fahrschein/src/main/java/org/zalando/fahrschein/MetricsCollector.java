package org.zalando.fahrschein;

public interface MetricsCollector {
    void markMessageReceived();

    void markEventsReceived(int size);

    void markErrorWhileConsuming();

    void markReconnection();

    void markMessageSuccessfullyProcessed();
}
