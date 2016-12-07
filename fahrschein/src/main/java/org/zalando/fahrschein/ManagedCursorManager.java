package org.zalando.fahrschein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.zalando.fahrschein.Preconditions.checkState;

public class ManagedCursorManager implements CursorManager {

    private static final Logger LOG = LoggerFactory.getLogger(ManagedCursorManager.class);

    static final class SubscriptionStream {
        private final String eventName;
        private final String subscriptionId;
        private String streamId;

        SubscriptionStream(String eventName, String subscriptionId) {
            this.eventName = eventName;
            this.subscriptionId = subscriptionId;
        }

        String getEventName() {
            return eventName;
        }

        String getSubscriptionId() {
            return subscriptionId;
        }

        String getStreamId() {
            return streamId;
        }

        void setStreamId(String streamId) {
            this.streamId = streamId;
        }
    }

    static final class CursorWrapper {
        private final List<Cursor> items;

        @JsonCreator
        CursorWrapper(@JsonProperty("items") List<Cursor> items) {
            this.items = items;
        }

        public List<Cursor> getItems() {
            return items;
        }
    }

    private final URI baseUri;
    private final ClientHttpRequestFactory clientHttpRequestFactory;
    private final ObjectMapper objectMapper;
    private final Map<String, SubscriptionStream> streams;

    public ManagedCursorManager(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory) {
        this.baseUri = baseUri;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.objectMapper = DefaultObjectMapper.INSTANCE;
        this.streams = new HashMap<>();
    }

    @Override
    public void addSubscription(Subscription subscription) {
        checkState(subscription.getEventTypes().size() == 1);
        final String eventName = subscription.getEventTypes().iterator().next();

        LOG.debug("Adding subscription [{}] to event [{}]", subscription.getId(), eventName);

        streams.put(eventName, new SubscriptionStream(eventName, subscription.getId()));
    }

    @Override
    public void addStreamId(Subscription subscription, String streamId) {
        checkState(subscription.getEventTypes().size() == 1);
        final String eventName = subscription.getEventTypes().iterator().next();

        LOG.debug("Adding stream id [{}] for subscription [{}] to event [{}]", streamId, subscription.getId(), eventName);

        streams.get(eventName).setStreamId(streamId);
    }

    @Override
    public void onSuccess(String eventName, Cursor cursor) throws IOException {

        final SubscriptionStream stream = streams.get(eventName);
        final String subscriptionId = stream.getSubscriptionId();
        final URI subscriptionUrl = baseUri.resolve(String.format("/subscriptions/%s/cursors", subscriptionId));

        LOG.debug("Committing cursors for subscription [{}] to event [{}] in partition [{}] with offset [{}]", subscriptionId, stream.getEventName(), cursor.getPartition(), cursor.getOffset());

        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(subscriptionUrl, HttpMethod.POST);

        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        request.getHeaders().put("X-Nakadi-StreamId", singletonList(stream.getStreamId()));

        try (OutputStream os = request.getBody()) {
            objectMapper.writeValue(os, new CursorWrapper(singletonList(cursor)));
        }

        try (final ClientHttpResponse response = request.execute()) {

            final int status = response.getStatusCode().value();
            if (status == 204) {
                LOG.debug("Successfully committed cursor for subscription [{}] to event [{}] in partition [{}] with offset [{}]", subscriptionId, eventName, cursor.getPartition(), cursor.getOffset());
            } else if (status == 200) {
                LOG.warn("Cursor for subscription [{}] to event [{}] in partition [{}] with offset [{}] was already committed", subscriptionId, eventName, cursor.getPartition(), cursor.getOffset());
            } else {
                // Error responses should already have been handled by ProblemHandlingClientHttpRequest, so we still treat this as success
                LOG.warn("Unexpected status code [{}] for subscription [{}] to event [{}] in partition [{}] with offset [{}]", status, subscriptionId, eventName, cursor.getPartition(), cursor.getOffset());
            }
        }
    }

    @Override
    public void onError(String eventName, Cursor cursor, Throwable throwable) {

    }

    @Override
    public Collection<Cursor> getCursors(String eventName) throws IOException {
        final SubscriptionStream stream = streams.get(eventName);
        final URI subscriptionUrl = baseUri.resolve(String.format("/subscriptions/%s/cursors", stream.getSubscriptionId()));

        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(subscriptionUrl, HttpMethod.GET);

        try (final ClientHttpResponse response = request.execute()) {
            try (InputStream is = response.getBody()) {
                final CursorWrapper cursorWrapper = objectMapper.readValue(is, CursorWrapper.class);
                return cursorWrapper.getItems();
            }
        }
    }
}
