package org.zalando.fahrschein;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.fahrschein.domain.Partition;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class NakadiClient {
    private static final TypeReference<List<Partition>> LIST_OF_PARTITIONS = new TypeReference<List<Partition>>() {
    };

    private final URI baseUri;
    private final ConnectionParameters connectionParameters;
    private final AccessTokenProvider accessTokenProvider;
    private final ObjectMapper objectMapper;
    private final CursorManager cursorManager;

    public NakadiClient(URI baseUri, ConnectionParameters connectionParameters, AccessTokenProvider accessTokenProvider, ObjectMapper objectMapper, CursorManager cursorManager) {
        this.baseUri = baseUri;
        this.connectionParameters = connectionParameters;
        this.accessTokenProvider = accessTokenProvider;
        this.objectMapper = objectMapper;
        this.cursorManager = cursorManager;
    }

    public List<Partition> getPartitions(String eventName) throws IOException, InterruptedException {
        final URL url = baseUri.resolve(String.format("/event-types/%s/partitions", eventName)).toURL();
        final InputStreamSupplier inputStreamSupplier = new UrlInputStreamSupplier(url);

        final InputStream inputStream = inputStreamSupplier.open(connectionParameters.withAuthorization("Bearer ".concat(accessTokenProvider.getAccessToken())));

        return objectMapper.readValue(inputStream, LIST_OF_PARTITIONS);
    }

    public <T> void listen(String eventName, Class<T> eventType, Listener<T> listener) throws IOException {
        listen(eventName, eventType, listener, new StreamParameters());
    }

    public <T> void listen(String eventName, Class<T> eventType, Listener<T> listener, StreamParameters streamParameters) throws IOException {
        final String queryString = streamParameters.toQueryString();
        final String path = String.format("/event-types/%s/events?%s", eventName, queryString);
        final URL url = baseUri.resolve(path).toURL();
        final InputStreamSupplier inputStreamSupplier = new ExponentialBackoffInputStreamSupplier(new UrlInputStreamSupplier(url), new ExponentialBackoffStrategy());
        final NakadiReader<T> nakadiReader = new NakadiReader<>(inputStreamSupplier, connectionParameters, accessTokenProvider, cursorManager, objectMapper, eventName, eventType, listener);

        nakadiReader.run();
    }
}
