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
import org.zalando.fahrschein.domain.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

import static org.zalando.fahrschein.Preconditions.checkArgument;
import static org.zalando.fahrschein.Preconditions.checkState;

public class NakadiClient {
    private static final Logger LOG = LoggerFactory.getLogger(NakadiClient.class);

    private static final TypeReference<List<Partition>> LIST_OF_PARTITIONS = new TypeReference<List<Partition>>() {
    };

    private final URI baseUri;
    private final ClientHttpRequestFactory clientHttpRequestFactory;
    private final ObjectMapper internalObjectMapper;
    private final ObjectMapper objectMapper;
    private final CursorManager cursorManager;

    public static NakadiClientBuilder builder(URI baseUri) {
        return new NakadiClientBuilder(baseUri);
    }

    NakadiClient(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, ObjectMapper objectMapper, CursorManager cursorManager) {
        this.baseUri = baseUri;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.objectMapper = objectMapper;
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

    public <T> void publish(String eventName, List<T> events) throws IOException {
        final URI uri = baseUri.resolve(String.format("/event-types/%s/events", eventName));
        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(uri, HttpMethod.POST);

        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try (final OutputStream body = request.getBody()) {
            objectMapper.writeValue(body, events);
        }

        try (final ClientHttpResponse response = request.execute()) {
            final MediaType contentType = response.getHeaders().getContentType();
            if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                try (final InputStream is = response.getBody()) {
                    final BatchItemResponse[] responses = internalObjectMapper.readValue(is, BatchItemResponse[].class);
                    final List<BatchItemResponse> failed = new ArrayList<>(responses.length);
                    for (BatchItemResponse batchItemResponse : responses) {
                        if (!BatchItemResponse.PublishingStatus.SUBMITTED.equals(batchItemResponse.getPublishingStatus())) {
                            failed.add(batchItemResponse);
                        }
                    }
                    if (!failed.isEmpty()) {
                        // TODO: attach corresponding events?
                        throw new EventPublishingException(failed.toArray(new BatchItemResponse[failed.size()]));
                    }
                }
            }
        }
    }

    /**
     * Create Subscription for one event type.
     *
     * @param applicationName
     * @param eventName
     * @param consumerGroup
     * @return
     * @throws IOException
     */
    public Subscription subscribe(String applicationName, String eventName, String consumerGroup) throws IOException {
        return subscribe(applicationName,  Collections.singleton(eventName), consumerGroup, SubscriptionRequest.Position.END, Collections.emptyList());
    }

    /**
     * Create Subscription for one event type with specified "read_from" position. If SubscriptionRequest.Position == "cursors"
     * then initialCursors parameter is required.
     *
     * @param applicationName
     * @param eventName
     * @param consumerGroup
     * @param readFrom
     * @return
     * @throws IOException
     */
    public Subscription subscribe(String applicationName, String eventName, String consumerGroup, SubscriptionRequest.Position readFrom, List<Cursor> initialCursors) throws IOException {
        return subscribe(applicationName,  Collections.singleton(eventName), consumerGroup, readFrom, initialCursors);
    }

    /**
     * Create Subscription for multiple event types.
     *
     * @param applicationName
     * @param eventNames
     * @param consumerGroup
     * @return
     * @throws IOException
     */
    public Subscription subscribe(String applicationName, Set<String> eventNames, String consumerGroup) throws IOException {
        return subscribe(applicationName, eventNames, consumerGroup, SubscriptionRequest.Position.END, Collections.emptyList());
    }

    /**
     * Create Subscription for multiple event types from specified "read_from" position.
     *
     * @param applicationName
     * @param eventNames
     * @param consumerGroup
     * @param readFrom
     * @return
     * @throws IOException
     */
    public Subscription subscribe(String applicationName, Set<String> eventNames, String consumerGroup, SubscriptionRequest.Position readFrom, List<Cursor> initialCursors) throws IOException {

        if(readFrom.equals(SubscriptionRequest.Position.CURSORS)){
            checkArgument(initialCursors != null, "Initial cursors are required for position: cursors");
            checkArgument(!initialCursors.isEmpty(), "Initial cursors are required for position: cursors");
        }

        final SubscriptionRequest subscription = new SubscriptionRequest(applicationName, eventNames, consumerGroup, readFrom, initialCursors);

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

        return new StreamBuilders.SubscriptionStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, subscription, null);
    }

    public StreamBuilder.LowLevelStreamBuilder stream(String eventName) {
        return new StreamBuilders.LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, null, eventName);
    }

}
