package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.metrics.MetricsCollector;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Optional;

public class NakadiReaderFactory {

    private final URI uri;
    private final ClientHttpRequestFactory clientHttpRequestFactory;
    private final BackoffStrategy backoffStrategy;
    private final CursorManager cursorManager;
    private final ObjectMapper objectMapper;
    private final MetricsCollector metricsCollector;

    public NakadiReaderFactory(final URI uri, final ClientHttpRequestFactory clientHttpRequestFactory, final BackoffStrategy backoffStrategy, final CursorManager cursorManager, final ObjectMapper objectMapper, @Nullable final MetricsCollector metricsCollector) {
        this.uri = uri;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.backoffStrategy = backoffStrategy;
        this.cursorManager = cursorManager;
        this.objectMapper = objectMapper;
        this.metricsCollector = metricsCollector;
    }

    public <T> NakadiReader createReader(final String eventName, final Optional<Subscription> subscription,
            final Class<T> eventType, final Listener<T> listener) {
        final NakadiReader<T> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper,
                eventName, subscription, eventType, listener);

        nakadiReader.setMetricsCollector(metricsCollector);

        return nakadiReader;
    }

}
