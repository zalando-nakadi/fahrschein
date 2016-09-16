package org.zalando.fahrschein;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.metrics.MetricsCollector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.zalando.fahrschein.metrics.NoMetricsCollector.NO_METRICS_COLLECTOR;

public class NakadiClient {
    private static final Logger LOG = LoggerFactory.getLogger(NakadiClient.class);

    private static final TypeReference<List<Partition>> LIST_OF_PARTITIONS = new TypeReference<List<Partition>>() {
    };

    private final URI baseUri;
    private final ClientHttpRequestFactory clientHttpRequestFactory;
    private final ObjectMapper objectMapper;
    private final CursorManager cursorManager;
    private final NakadiReaderFactory nakadiReaderFactory;
    private final ScheduledExecutorService executor;

    public NakadiClient(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, BackoffStrategy backoffStrategy, ObjectMapper objectMapper, CursorManager cursorManager) {
        this(baseUri, clientHttpRequestFactory, backoffStrategy, objectMapper, cursorManager, NO_METRICS_COLLECTOR);

    }
    public NakadiClient(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, BackoffStrategy backoffStrategy, ObjectMapper objectMapper, CursorManager cursorManager, MetricsCollector metricsCollector) {
        this.baseUri = baseUri;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.objectMapper = objectMapper;
        this.cursorManager = cursorManager;
        this.nakadiReaderFactory = new NakadiReaderFactory(clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, metricsCollector);
        this.executor = Executors.newScheduledThreadPool(1);
    }

    public List<Partition> getPartitions(String eventName) throws IOException {
        final URI uri = baseUri.resolve(String.format("/event-types/%s/partitions", eventName));
        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(uri, HttpMethod.GET);
        try (final ClientHttpResponse response = request.execute()) {
            try (final InputStream is = response.getBody()) {
                return objectMapper.readValue(is, LIST_OF_PARTITIONS);
            }
        }
    }

    public Subscription subscribe(String applicationName, String eventName, String consumerGroup) throws IOException {
        final Subscription subscription = new Subscription(applicationName, Collections.singleton(eventName), consumerGroup);

        final URI uri = baseUri.resolve("/subscriptions");
        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(uri, HttpMethod.POST);

        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try (final OutputStream os = request.getBody()) {
            objectMapper.writeValue(os, subscription);
        }

        try (final ClientHttpResponse response = request.execute()) {
            try (final InputStream is = response.getBody()) {
                final Subscription subscriptionResponse = objectMapper.readValue(is, Subscription.class);
                LOG.info("Created subscription for event {} with id [{}]", subscription.getEventTypes(), subscriptionResponse.getId());
                cursorManager.addSubscription(subscriptionResponse);
                return subscriptionResponse;
            }
        }
    }

    public <T> void listen(Subscription subscription, Class<T> eventType, Listener<T> listener, StreamParameters streamParameters) throws IOException {
        final String eventName = Iterables.getOnlyElement(subscription.getEventTypes());
        final String queryString = streamParameters.toQueryString();
        final URI uri = baseUri.resolve(String.format("/subscriptions/%s/events?%s", subscription.getId(), queryString));

        listen(uri, eventName, eventType, listener, -1, TimeUnit.SECONDS);
    }

    public <T> void listen(String eventName, Class<T> eventType, Listener<T> listener, StreamParameters streamParameters) throws IOException {
        listen(eventName, eventType, listener, streamParameters, -1, TimeUnit.SECONDS);
    }

    public <T> void listen(String eventName, Class<T> eventType, Listener<T> listener, StreamParameters streamParameters, long lockTimeout, TimeUnit timeoutUnit) throws IOException {
        final String queryString = streamParameters.toQueryString();
        final URI uri = baseUri.resolve(String.format("/event-types/%s/events?%s", eventName, queryString));

        listen(uri, eventName, eventType, listener, lockTimeout, timeoutUnit);
    }

    private <T> void listen(URI uri, String eventName, Class<T> eventType, Listener<T> listener, long lockTimeout, TimeUnit timeoutUnit) throws IOException {
        final NakadiReader<T> nakadiReader = nakadiReaderFactory.createReader(uri, eventName, Optional.<Subscription>empty(), eventType, listener);

        if (lockTimeout > 0) {
            final Thread currentThread = Thread.currentThread();
            executor.schedule(currentThread::interrupt, lockTimeout, timeoutUnit);
        }

        nakadiReader.run();
    }
}
