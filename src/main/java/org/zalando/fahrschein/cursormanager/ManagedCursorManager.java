package org.zalando.fahrschein.cursormanager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagedCursorManager implements CursorManager {

    private static final Logger LOG = LoggerFactory.getLogger(ManagedCursorManager.class);
    private static final TypeReference<List<Cursor>> LIST_OF_CURSORS = new TypeReference<List<Cursor>>() {
    };

    private final URI baseUri;
    private final ClientHttpRequestFactory clientHttpRequestFactory;
    private final ObjectMapper objectMapper;
    private final Map<String, Subscription> subscriptions;

    public ManagedCursorManager(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, ObjectMapper objectMapper) {
        this.baseUri = baseUri;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.objectMapper = objectMapper;
        this.subscriptions = new HashMap<>();
    }

    @Override
    public void addSubscription(Subscription subscription) {
        final String eventName = Iterables.getOnlyElement(subscription.getEventTypes());
        subscriptions.put(eventName, subscription);
    }

    @Override
    public void onSuccess(String eventName, Cursor cursor) throws IOException {
        final Subscription subscription = subscriptions.get(eventName);
        final URI subscriptionUrl = baseUri.resolve(String.format("/subscriptions/%s/cursors", subscription.getId()));

        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(subscriptionUrl, HttpMethod.PUT);

        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try (OutputStream os = request.getBody()) {
            objectMapper.writeValue(os, Collections.singleton(cursor));
        }

        try (final ClientHttpResponse response = request.execute()) {

            if (response.getStatusCode().value() == HttpStatus.NO_CONTENT.value()) {
                LOG.warn("Cursor for event [{}] in partition [{}] with offset [{}] was already committed", eventName, cursor.getPartition(), cursor.getOffset());
            } else if (response.getStatusCode().is2xxSuccessful()) {
                LOG.debug("Successfully committed cursor for event [{}] in partition [{}] with offset [{}]", eventName, cursor.getPartition(), cursor.getOffset());
            }
        }
    }

    @Override
    public void onError(String eventName, Cursor cursor, Throwable throwable) {

    }

    @Override
    public Collection<Cursor> getCursors(String eventName) throws IOException {
        final Subscription subscription = subscriptions.get(eventName);
        final URI subscriptionUrl = baseUri.resolve(String.format("/subscriptions/%s/cursors", subscription.getId()));

        final ClientHttpRequest request = clientHttpRequestFactory.createRequest(subscriptionUrl, HttpMethod.GET);

        try (final ClientHttpResponse response = request.execute()) {
            try (InputStream is = response.getBody()) {
                return objectMapper.readValue(is, LIST_OF_CURSORS);
            }
        }
    }
}
