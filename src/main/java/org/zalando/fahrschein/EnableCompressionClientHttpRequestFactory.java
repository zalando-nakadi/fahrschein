package org.zalando.fahrschein;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.io.IOException;
import java.net.URI;

public class EnableCompressionClientHttpRequestFactory implements ClientHttpRequestFactory {
    private final ClientHttpRequestFactory delegate;

    public EnableCompressionClientHttpRequestFactory(final ClientHttpRequestFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public ClientHttpRequest createRequest(final URI uri, final HttpMethod httpMethod) throws IOException {
        final ClientHttpRequest request = delegate.createRequest(uri, httpMethod);
        request.getHeaders().add("Accept-Encoding", "gzip");
        return request;
    }
}
