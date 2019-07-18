package org.zalando.fahrschein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.zalando.fahrschein.NakadiClientBuilder.wrapClientHttpRequestFactory;
import static java.util.Collections.singletonList;

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
    private final RequestFactory clientHttpRequestFactory;
    private final ObjectMapper objectMapper;
    private final Map<String, SubscriptionStream> streams;

    public ManagedCursorManager(URI baseUri, RequestFactory clientHttpRequestFactory, AuthorizationProvider authorizationProvider) {
        this(baseUri, wrapClientHttpRequestFactory(clientHttpRequestFactory, authorizationProvider), true);
    }

    public ManagedCursorManager(URI baseUri, RequestFactory clientHttpRequestFactory) {
        this(baseUri, wrapClientHttpRequestFactory(clientHttpRequestFactory, null), true);
    }

    ManagedCursorManager(URI baseUri, RequestFactory clientHttpRequestFactory, boolean clientHttpRequestFactoryIsAlreadyWrapped) {
        this.baseUri = baseUri;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.objectMapper = DefaultObjectMapper.INSTANCE;
        this.streams = new ConcurrentHashMap<>();
    }

    @Override
    public void addSubscription(Subscription subscription) {
        for(String eventName: subscription.getEventTypes()){
            LOG.debug("Adding subscription [{}] to event [{}]", subscription.getId(), eventName);
            streams.put(eventName, new SubscriptionStream(eventName, subscription.getId()));
        }
    }

    @Override
    public void addStreamId(Subscription subscription, String streamId) {
        for(String eventName: subscription.getEventTypes()) {
            LOG.debug("Adding stream id [{}] for subscription [{}] to event [{}]", streamId, subscription.getId(), eventName);
            streams.get(eventName).setStreamId(streamId);
        }
    }

    @Override
    public void onSuccess(String eventName, Cursor cursor) throws IOException {

        final SubscriptionStream stream = streams.get(eventName);
        final String subscriptionId = stream.getSubscriptionId();
        final URI subscriptionUrl = baseUri.resolve(String.format("/subscriptions/%s/cursors", subscriptionId));

        LOG.debug("Committing cursors for subscription [{}] to event [{}] in partition [{}] with offset [{}]", subscriptionId, stream.getEventName(), cursor.getPartition(), cursor.getOffset());

        final Request request = clientHttpRequestFactory.createRequest(subscriptionUrl, "POST");

        request.getHeaders().setContentType(ContentType.APPLICATION_JSON);
        request.getHeaders().put("X-Nakadi-StreamId", stream.getStreamId());

        try (OutputStream os = request.getBody()) {
            objectMapper.writeValue(os, new CursorWrapper(singletonList(cursor)));
        }

        try (final Response response = request.execute()) {

            final int status = response.getStatusCode();
            if (status == 204) {
                LOG.debug("Successfully committed cursor for subscription [{}] to event [{}] in partition [{}] with offset [{}]", subscriptionId, eventName, cursor.getPartition(), cursor.getOffset());
            } else if (status == 200) {
                LOG.warn("Cursor for subscription [{}] to event [{}] in partition [{}] with offset [{}] was already committed", subscriptionId, eventName, cursor.getPartition(), cursor.getOffset());
            } else {
                throw new IOException(String.format("Unexpected status code [%s] for subscription [%s] to event [%s]", status, subscriptionId, eventName));
            }
        }
    }

    @Override
    public void onSuccess(String eventName, List<Cursor> cursors) throws IOException {
        for (Cursor cursor : cursors) {
            onSuccess(eventName, cursor);
        }
    }

    @Override
    public Collection<Cursor> getCursors(String eventName) throws IOException {
        final SubscriptionStream stream = streams.get(eventName);
        final URI subscriptionUrl = baseUri.resolve(String.format("/subscriptions/%s/cursors", stream.getSubscriptionId()));

        final Request request = clientHttpRequestFactory.createRequest(subscriptionUrl, "GET");

        try (final Response response = request.execute()) {
            try (InputStream is = response.getBody()) {
                final CursorWrapper cursorWrapper = objectMapper.readValue(is, CursorWrapper.class);
                return cursorWrapper.getItems();
            }
        }
    }
}
