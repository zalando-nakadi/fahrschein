package org.zalando.fahrschein;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.domain.Authorization;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.domain.SubscriptionRequest;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.RequestHandler;
import org.zalando.fahrschein.http.api.Response;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.zalando.fahrschein.Preconditions.checkArgument;
import static org.zalando.fahrschein.Preconditions.checkState;

/**
 * General implementation of the Nakadi Client used within this Library.
 */
public class NakadiClient {
    private static final Logger LOG = LoggerFactory.getLogger(NakadiClient.class);

    private static final TypeReference<List<Partition>> LIST_OF_PARTITIONS = new TypeReference<List<Partition>>() {
    };

    private final URI baseUri;
    private final RequestFactory requestFactory;
    private final ObjectMapper internalObjectMapper;
    private final ObjectMapper objectMapper;
    private final CursorManager cursorManager;
    private final List<RequestHandler> requestHandlers;

    /**
     * Returns a new Builder that will make use of the given {@code RequestFactory}.
     *
     * @param baseUri that we will send requests to
     * @param requestFactory that we use for the execution of our HTTP Requests.
     * @return A builder to initialize the client. Can be further modified later.
     */
    public static NakadiClientBuilder builder(URI baseUri, RequestFactory requestFactory) {
        return new NakadiClientBuilder(baseUri, requestFactory);
    }

    NakadiClient(URI baseUri, RequestFactory requestFactory, ObjectMapper objectMapper, CursorManager cursorManager) {
        this.baseUri = baseUri;
        this.requestFactory = requestFactory;
        this.objectMapper = objectMapper;
        this.internalObjectMapper = DefaultObjectMapper.INSTANCE;
        this.cursorManager = cursorManager;
        this.requestHandlers = new ArrayList<>();
    }

    NakadiClient(URI baseUri, RequestFactory requestFactory, ObjectMapper objectMapper, CursorManager cursorManager, List<RequestHandler> requestHandlers) {
        this.baseUri = baseUri;
        this.requestFactory = requestFactory;
        this.objectMapper = objectMapper;
        this.internalObjectMapper = DefaultObjectMapper.INSTANCE;
        this.cursorManager = cursorManager;
        this.requestHandlers = requestHandlers;
    }


    /**
     * Resolves a list of partitions for the given eventName.
     * @param eventName that we want to resolve the partitions for.
     * @return {@code List<Partition>} or {@code null} in
     * @throws IOException in case of network issues.
     */
    public List<Partition> getPartitions(String eventName) throws IOException {
        final URI uri = baseUri.resolve("/event-types/" + eventName + "/partitions");
        final Request request = requestFactory.createRequest(uri, "GET");
        try (final Response response = request.execute()) {
            try (final InputStream is = response.getBody()) {
                return internalObjectMapper.readValue(is, LIST_OF_PARTITIONS);
            }
        }
    }

    /**
     * Writes the given events to the endpoint provided by the eventName.
     * @param eventName where the event should be written to
     * @param events that should be written
     * @param <T> Type of the Event
     * @throws IOException in case we fail to reach Nakadi
     * @throws EventPublishingException In case Nakadi returns an Erroneous response
     */
    public <T> void publish(String eventName, List<T> events) throws EventPublishingException, IOException {
        final URI uri = baseUri.resolve(String.format(Locale.ENGLISH, "/event-types/%s/events", eventName));
        final Request request = requestFactory.createRequest(uri, "POST");

        request.getHeaders().setContentType(ContentType.APPLICATION_JSON);

        try (final OutputStream body = request.getBody()) {
            objectMapper.writeValue(body, events);
        }

        requestHandlers.forEach(requestHandler -> requestHandler.beforeExecute(request));
        try (final Response response = request.execute()) {
            LOG.debug("Successfully published [{}] events for [{}]", events.size(), eventName);
            requestHandlers.forEach(requestHandler -> requestHandler.afterExecute(request, response));
        } catch (Throwable t) {
            requestHandlers.forEach(requestHandler -> requestHandler.onError(request, t));
            throw t;
        }
    }

    /**
     * Create a subscription for a single event type.
     *
     * @deprecated Use the {@link SubscriptionBuilder} and {@link NakadiClient#subscription(String, String)} instead.
     */
    @Deprecated
    public Subscription subscribe(String applicationName, String eventName, String consumerGroup) throws IOException {
        return subscription(applicationName, eventName).withConsumerGroup(consumerGroup).subscribe();
    }

    /**
     * Build a subscription for a single event type.
     */
    public SubscriptionBuilder subscription(String applicationName, String eventName) throws IOException {
        return new SubscriptionBuilder(this, applicationName, Collections.singleton(eventName));
    }

    /**
     * Build a subscription for multiple event types.
     */
    public SubscriptionBuilder subscription(String applicationName, Set<String> eventNames) throws IOException {
        return new SubscriptionBuilder(this, applicationName, eventNames);
    }

    /**
     * Delete subscription based on subscription ID.
     */
    public void deleteSubscription(String subscriptionId) throws IOException {
        checkArgument(!subscriptionId.isEmpty(), "Subscription ID cannot be empty.");

        final URI uri = baseUri.resolve(String.format(Locale.ENGLISH, "/subscriptions/%s", subscriptionId));
        final Request request = requestFactory.createRequest(uri, "DELETE");

        request.getHeaders().setContentType(ContentType.APPLICATION_JSON);

        try (final Response response = request.execute()) {

            final int status = response.getStatusCode();
            if (status == 204) {
                LOG.debug("Successfully deleted subscription [{}]", subscriptionId);
            }
        }

    }

    Subscription subscribe(String applicationName, Set<String> eventNames, String consumerGroup, SubscriptionRequest.Position readFrom, @Nullable List<Cursor> initialCursors, @Nullable Authorization authorization) throws IOException {

        checkArgument(readFrom != SubscriptionRequest.Position.CURSORS || (initialCursors != null && !initialCursors.isEmpty()), "Initial cursors are required for position: cursors");

        final SubscriptionRequest subscription = new SubscriptionRequest(applicationName, eventNames, consumerGroup, readFrom, initialCursors, authorization);

        final URI uri = baseUri.resolve("/subscriptions");
        final Request request = requestFactory.createRequest(uri, "POST");

        request.getHeaders().setContentType(ContentType.APPLICATION_JSON);

        try (final OutputStream os = request.getBody()) {
            internalObjectMapper.writeValue(os, subscription);
        }

        try (final Response response = request.execute()) {
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

        return new StreamBuilders.SubscriptionStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, subscription);
    }

    public StreamBuilder.LowLevelStreamBuilder stream(String eventName) {
        return new StreamBuilders.LowLevelStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, eventName);
    }

}
