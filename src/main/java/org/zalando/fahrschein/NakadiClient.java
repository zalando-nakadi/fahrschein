package org.zalando.fahrschein;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
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
import org.zalando.fahrschein.domain.SubscriptionRequest;
import org.zalando.fahrschein.metrics.MetricsCollector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public NakadiClient(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, BackoffStrategy backoffStrategy, ObjectMapper objectMapper, CursorManager cursorManager) {
        this(baseUri, clientHttpRequestFactory, backoffStrategy, objectMapper, cursorManager, NO_METRICS_COLLECTOR);
    }

    public NakadiClient(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, BackoffStrategy backoffStrategy, ObjectMapper objectMapper, CursorManager cursorManager, MetricsCollector metricsCollector) {
        this.baseUri = baseUri;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.objectMapper = createObjectMapper();
        this.cursorManager = cursorManager;
        this.nakadiReaderFactory = new NakadiReaderFactory(clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, metricsCollector);
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModules(new Jdk8Module(), new ParameterNamesModule(), new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
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
        final SubscriptionRequest subscription = new SubscriptionRequest(applicationName, Collections.singleton(eventName), consumerGroup);

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

        final NakadiReader<T> nakadiReader = nakadiReaderFactory.createReader(uri, eventName, Optional.of(subscription), eventType, listener);

        nakadiReader.run();
    }

    public <T> void listen(String eventName, Class<T> eventType, Listener<T> listener, StreamParameters streamParameters) throws IOException {
        final String queryString = streamParameters.toQueryString();
        final URI uri = baseUri.resolve(String.format("/event-types/%s/events?%s", eventName, queryString));

        final NakadiReader<T> nakadiReader = nakadiReaderFactory.createReader(uri, eventName, Optional.empty(), eventType, listener);

        nakadiReader.run();
    }
}
