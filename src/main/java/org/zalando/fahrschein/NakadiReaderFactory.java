package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.domain.Subscription;

import java.net.URI;
import java.util.Optional;

public interface NakadiReaderFactory {

    <T> NakadiReader nakadiReader(URI uri, ClientHttpRequestFactory clientHttpRequestFactory,
            BackoffStrategy backoffStrategy, CursorManager cursorManager, ObjectMapper objectMapper, String eventName,
            Optional<Subscription> subscription, Class<T> eventClass, Listener<T> listener);

}