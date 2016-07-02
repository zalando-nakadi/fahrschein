package org.zalando.fahrschein;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;

public class CloseableHttpComponentsClientRequestFactory implements ClientHttpRequestFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CloseableHttpComponentsClientRequestFactory.class);

    private final HttpComponentsClientHttpRequestFactory delegate;

    public CloseableHttpComponentsClientRequestFactory(HttpComponentsClientHttpRequestFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        final ClientHttpRequest request = delegate.createRequest(uri, httpMethod);
        return new ClientHttpRequest() {
            @Override
            public ClientHttpResponse execute() throws IOException {
                final ClientHttpResponse response = request.execute();

                return new ClientHttpResponse() {
                    @Override
                    public HttpStatus getStatusCode() throws IOException {
                        return response.getStatusCode();
                    }

                    @Override
                    public int getRawStatusCode() throws IOException {
                        return response.getRawStatusCode();
                    }

                    @Override
                    public String getStatusText() throws IOException {
                        return response.getStatusText();
                    }

                    @Override
                    public InputStream getBody() throws IOException {
                        return response.getBody();
                    }

                    @Override
                    public HttpHeaders getHeaders() {
                        return response.getHeaders();
                    }

                    @Override
                    public void close() {
                        try {
                            final Field field = response.getClass().getDeclaredField("httpResponse");
                            field.setAccessible(true);
                            final CloseableHttpResponse closeableHttpResponse = (CloseableHttpResponse) field.get(response);
                            try {
                                closeableHttpResponse.close();
                            } catch (IOException e) {
                                LOG.debug("IOException while trying to close response", e);
                            }
                        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
                            LOG.warn("Could not release underlying response, trying normal close", e);
                            response.close();
                        }

                    }
                };
            }

            @Override
            public OutputStream getBody() throws IOException {
                return request.getBody();
            }

            @Override
            public HttpMethod getMethod() {
                return request.getMethod();
            }

            @Override
            public URI getURI() {
                return request.getURI();
            }

            @Override
            public HttpHeaders getHeaders() {
                return request.getHeaders();
            }
        };
    }
}
