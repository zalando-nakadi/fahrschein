package org.zalando.fahrschein;

import java.net.URI;
import java.util.Optional;

import javax.annotation.Nullable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.metrics.MetricsCollector;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    <T> NakadiReader<T> createReader(final URI uri, final String eventName, final Optional<Subscription> subscription,
            final Class<T> eventType, final Listener<T> listener) {

        return new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper,
                eventName, subscription, eventType, listener, metricsCollector);
    }

    <T> NakadiReader createReader(final URI uri, final String eventName, final Optional<Subscription> subscription,
            final Class<T> eventClass, JavaType eventType, final Listener<T> listener, @Nullable final MetricsCollector metricsCollector) {
    	final NakadiReader<T> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper,
                eventName, subscription, eventClass, eventType, listener, metricsCollector);

        return nakadiReader;
    }
}
