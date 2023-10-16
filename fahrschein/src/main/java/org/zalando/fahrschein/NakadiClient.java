package org.zalando.fahrschein;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.domain.Authorization;
import org.zalando.fahrschein.domain.BatchItemResponse;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Event;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.domain.SubscriptionRequest;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.Response;

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
    private final LinkedList<EventPublishingHandler> eventPublishingHandlers;
    private final BackoffStrategy backoffStrategy;


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

    NakadiClient(URI baseUri, RequestFactory requestFactory, ObjectMapper objectMapper, CursorManager cursorManager,
            final BackoffStrategy backoffStrategy) {
        this.baseUri = baseUri;
        this.requestFactory = requestFactory;
        this.objectMapper = objectMapper;
        this.internalObjectMapper = DefaultObjectMapper.INSTANCE;
        this.cursorManager = cursorManager;
        this.eventPublishingHandlers = new LinkedList<>();
        this.backoffStrategy = backoffStrategy;
    }

    NakadiClient(URI baseUri, RequestFactory requestFactory, ObjectMapper objectMapper, CursorManager cursorManager,
            List<EventPublishingHandler> eventPublishingHandlers, final BackoffStrategy backoffStrategy) {
        this.baseUri = baseUri;
        this.requestFactory = requestFactory;
        this.objectMapper = objectMapper;
        this.internalObjectMapper = DefaultObjectMapper.INSTANCE;
        this.cursorManager = cursorManager;
        this.eventPublishingHandlers = new LinkedList<>(eventPublishingHandlers);
        this.backoffStrategy = backoffStrategy;

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
     *
     * <p>In case of a partial success, Fahrschein will throw an {@link EventPersistenceException}`, which is retryable.
     * In case of validation errors, which are complete failures, and should not be retried, it will throw an {@link EventValidationException}.
     * The exceptions contain a list of individual {@link BatchItemResponse}s in order of the batch items sent to Nakadi.
     * </p>
     * <p>These objects have the event-ids of the failed event, a publishingStatus (failed/aborted/submitted), the step where it failed and a detail string.
     * </p>
     *
     * <p>Recommendation: Implement a retry-with-backoff handler for {@link EventPersistenceException}s, for which, depending on
     * strong ordering consistency requirements, you either retry the full batch, or retry only the failed/aborted events.</p>
     *
     * @param eventName where the event should be written to
     * @param events that should be written
     * @param <T> Type of the Event
     * @throws IOException in case of network errors when calling Nakadi.
     * @throws EventValidationException in case Nakadi rejects the batch in event validation phase - should not be retried until either the event type schema or the event payload has been corrected.
     * @throws EventPersistenceException in case Nakadi fails to persist the batch (partially). Retryable (see recommendation above).
     */
    public <T> void publish(String eventName, List<T> events) throws EventValidationException, EventPersistenceException, IOException  {
        try {
            send(eventName,events,null,null);
            LOG.debug("Successfully published [{}] events for [{}]", events.size(), eventName);
        } catch (Throwable t) {
            eventPublishingHandlers.descendingIterator().forEachRemaining(handler -> handler.onError(events, t));
            throw t;
        }
    }

    /**
     * Writes a batch of events to the specified endpoint using Nakadi, with support for retry and backoff.
     *
     * This method sends a batch of events to the given event endpoint while providing options for retry behavior and backoff in case of failures.
     *
     * @param eventName The name of the event to which the batch should be written.
     * @param events A list of events to be included in the batch.
     * @param partialRetry A flag indicating whether to retry the entire batch or only the failed/aborted events.
     * @param <T> The type of the events being published.
     * @throws IOException If an IO error occurs while interacting with Nakadi.
     * @throws BackoffException If the retry limit is exhausted or the thread is interrupted during the retry process.
     */
    public <T> void publishWithRetry(String eventName, List<T> events, boolean partialRetry) throws IOException, BackoffException {
        checkState(backoffStrategy != null, "BackoffStrategy is required for retry");
        try {
            try {
                send(eventName, events, partialRetry,null );
                LOG.debug("Successfully published [{}] events for [{}]", events.size(), eventName);
            } catch (final EventPersistenceException ex) {
                ExceptionAwareCallable<Void> retryableOperation = (retryCount, exception) -> {
                    send(eventName, events, partialRetry, exception);
                    return null;
                };
                try {
                    backoffStrategy.call(ex, 0, retryableOperation);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Throwable t) {
            eventPublishingHandlers.descendingIterator().forEachRemaining(handler -> handler.onError(List.of(), t));
            throw t;
        } finally {
            eventPublishingHandlers.descendingIterator().forEachRemaining(EventPublishingHandler::afterPublish);
        }
    }

    /**
     * Send a batch of events to the specified Nakadi event endpoint with optional support for partial retries.
     *
     * @param eventName     The name of the event to which the batch should be sent.
     * @param events        A list of events to be included in the batch.
     * @param partialRetry  A flag indicating whether to retry only the failed/aborted events (if true).
     * @param ex            An exception containing information about the failed events (used when partialRetry is true).
     * @throws IOException If an IO error occurs during the communication with Nakadi.
     */
    private <T> void send(final String eventName, List<T> events, final Boolean partialRetry, final EventPersistenceException ex) throws IOException {
        final URI uri = baseUri.resolve(String.format(Locale.ENGLISH, "/event-types/%s/events", eventName));
        final Request request = requestFactory.createRequest(uri, "POST");

        request.getHeaders().setContentType(ContentType.APPLICATION_JSON);

        // If partialRetry is enabled and there are failed events, filter the events to include only the failed ones
        if (partialRetry != null && partialRetry && ex != null) {
            final List<String> eids = Arrays.stream(ex.getResponses())
                    .filter(r -> !r.getPublishingStatus().equals(BatchItemResponse.PublishingStatus.SUBMITTED))
                    .map(BatchItemResponse::getEid)
                    .toList();
            events = events.stream()
                    .filter(e -> eids.contains(((Event) e).getMetadata().getEid()))
                    .toList();
        }
        final List<T> finalEvents = events;
        try (final OutputStream body = request.getBody()) {
            objectMapper.writeValue(body, finalEvents);
        }
        Response response = null;
        try {
            eventPublishingHandlers.forEach(handler -> handler.onPublish(eventName, finalEvents));
            response = request.execute();
        } finally {
            if (response != null) {
                response.close();
            }
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

    Subscription subscribe(String applicationName, Set<String> eventNames, String subscriptionId){
        final Subscription subscription = new Subscription(subscriptionId, applicationName, eventNames);
        LOG.info("Using subscription ID [{}] for event {}", subscriptionId, eventNames);
        cursorManager.addSubscription(subscription);
        return subscription;
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
