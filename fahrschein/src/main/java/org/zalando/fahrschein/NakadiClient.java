package org.zalando.fahrschein;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.zalando.fahrschein.Preconditions.checkState;

public class NakadiClient {
    private static final Logger LOG = LoggerFactory.getLogger(NakadiClient.class);

    private static final TypeReference<List<Partition>> LIST_OF_PARTITIONS = new TypeReference<List<Partition>>() {
    };

    private final URI baseUri;
    private final ClientHttpRequestFactory clientHttpRequestFactory;
    private final ObjectMapper internalObjectMapper;
    private final CursorManager cursorManager;

    public static NakadiClientBuilder builder(URI baseUri) {
        return new NakadiClientBuilder(baseUri);
    }

    NakadiClient(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager) {
        this.baseUri = baseUri;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.internalObjectMapper = DefaultObjectMapper.INSTANCE;
        this.cursorManager = cursorManager;
    }

    public List<Partition> getPartitions(String eventName) throws IOException {
        final URI uri = baseUri.resolve(String.format("/event-types/%s/partitions", eventName));
        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(uri, HttpMethod.GET);
        try (final ClientHttpResponse response = request.execute()) {
            try (final InputStream is = response.getBody()) {
                return internalObjectMapper.readValue(is, LIST_OF_PARTITIONS);
            }
        }
    }

    public Subscription subscribe(String applicationName, String eventName, String consumerGroup) throws IOException {
        final SubscriptionRequest subscription = new SubscriptionRequest(applicationName, Collections.singleton(eventName), consumerGroup);

        final URI uri = baseUri.resolve("/subscriptions");
        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(uri, HttpMethod.POST);

        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try (final OutputStream os = request.getBody()) {
            internalObjectMapper.writeValue(os, subscription);
        }

        try (final ClientHttpResponse response = request.execute()) {
            try (final InputStream is = response.getBody()) {
                final Subscription subscriptionResponse = internalObjectMapper.readValue(is, Subscription.class);
                LOG.info("Created subscription for event {} with id [{}]", subscription.getEventTypes(), subscriptionResponse.getId());
                cursorManager.addSubscription(subscriptionResponse);
                return subscriptionResponse;
            }
        }
    }

    public StreamBuilder.SubscriptionStreamBuilder stream(Subscription subscription) {
        checkState(cursorManager instanceof ManagedCursorManager, "Subscription api requires a ManagedCursorManager");

        return new StreamBuilders.SubscriptionStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, subscription);
    }

    public StreamBuilder.LowLevelStreamBuilder stream(String eventName) {
        return new StreamBuilders.LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, eventName);
    }

}
