package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.metrics.MetricsCollector;

import java.net.URI;
import java.util.Optional;

class NakadiReaderFactory {

    private final ClientHttpRequestFactory clientHttpRequestFactory;
    private final BackoffStrategy backoffStrategy;
    private final CursorManager cursorManager;
    private final ObjectMapper objectMapper;
    private final MetricsCollector metricsCollector;

    NakadiReaderFactory(final ClientHttpRequestFactory clientHttpRequestFactory, final BackoffStrategy backoffStrategy, final CursorManager cursorManager, final ObjectMapper objectMapper, MetricsCollector metricsCollector) {
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.backoffStrategy = backoffStrategy;
        this.cursorManager = cursorManager;
        this.objectMapper = objectMapper;
        this.metricsCollector = metricsCollector;
    }

    <T> NakadiReader<T> createReader(final URI uri, final String eventName, final Optional<Subscription> subscription, final Optional<Lock> lock,
            final Class<T> eventType, final Listener<T> listener) {

        return new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper,
                eventName, subscription, lock, eventType, listener, metricsCollector);
    }

}
