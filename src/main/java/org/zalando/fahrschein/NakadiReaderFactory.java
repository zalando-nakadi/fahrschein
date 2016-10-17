package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.metrics.MetricsCollector;

import java.net.URI;
import java.util.Optional;

class NakadiReaderFactory {

    private final ClientHttpRequestFactory clientHttpRequestFactory;
    private final BackoffStrategy backoffStrategy;
    private final CursorManager cursorManager;
    private final ObjectMapper objectMapper;

    NakadiReaderFactory(final ClientHttpRequestFactory clientHttpRequestFactory, final BackoffStrategy backoffStrategy, final CursorManager cursorManager, final ObjectMapper objectMapper) {
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.backoffStrategy = backoffStrategy;
        this.cursorManager = cursorManager;
        this.objectMapper = objectMapper;
    }

    <T> NakadiReader<T> createReader(final URI uri, final String eventName, final Optional<Subscription> subscription,
            final Class<T> eventType, final Listener<T> listener, final MetricsCollector metricsCollector) {

        return new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper,
                eventName, subscription, eventType, listener, metricsCollector);
    }

}
