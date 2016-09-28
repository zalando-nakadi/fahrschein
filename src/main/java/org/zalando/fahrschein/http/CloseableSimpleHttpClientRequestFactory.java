package org.zalando.fahrschein.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URI;

public class CloseableSimpleHttpClientRequestFactory implements ClientHttpRequestFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CloseableSimpleHttpClientRequestFactory.class);

    private final SimpleClientHttpRequestFactory delegate;

    public CloseableSimpleHttpClientRequestFactory(SimpleClientHttpRequestFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        final ClientHttpRequest request = delegate.createRequest(uri, httpMethod);
        return new DelegatingClientHttpRequest(request) {
            @Override
            public ClientHttpResponse execute() throws IOException {
                final ClientHttpResponse response = super.execute();
                return new DelegatingClientHttpResponse(response) {
                    @Override
                    public void close() {
                        final ClientHttpResponse response = getDelegate();
                        try {
                            final Field field = response.getClass().getDeclaredField("responseStream");
                            field.setAccessible(true);
                            final Closeable responseStream = (Closeable) field.get(response);
                            try {
                                responseStream.close();
                            } catch (IOException e) {
                                LOG.debug("IOException while trying to close response", e);
                            }
                        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
                            LOG.warn("Could not close underlying response stream, trying normal close", e);
                            super.close();
                            return;
                        }
                        try {
                            final Field field = response.getClass().getDeclaredField("connection");
                            field.setAccessible(true);
                            final HttpURLConnection connection = (HttpURLConnection) field.get(response);
                            connection.disconnect();
                        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
                            LOG.warn("Could not disconnect underlying connection, trying normal close", e);
                            super.close();
                        }
                    }
                };
            }
        };
    }
}
