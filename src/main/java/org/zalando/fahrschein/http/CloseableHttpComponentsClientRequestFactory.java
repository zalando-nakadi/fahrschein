package org.zalando.fahrschein.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;

public class CloseableHttpComponentsClientRequestFactory implements ClientHttpRequestFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CloseableHttpComponentsClientRequestFactory.class);

    private final HttpComponentsClientHttpRequestFactory delegate;

    public CloseableHttpComponentsClientRequestFactory(final HttpComponentsClientHttpRequestFactory delegate) {
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
                        try {
                            final ClientHttpResponse response = getDelegate();
                            final Field field = response.getClass().getDeclaredField("httpResponse");
                            field.setAccessible(true);
                            final Closeable closeableHttpResponse = (Closeable) field.get(response);
                            try {
                                closeableHttpResponse.close();
                            } catch (IOException e) {
                                LOG.debug("IOException while trying to close response", e);
                            }
                        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
                            LOG.warn("Could not release underlying response, trying normal close", e);
                            super.close();
                        }
                    }
                };
            }
        };
    }

}
