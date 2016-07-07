package org.zalando.fahrschein;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.io.IOException;
import java.net.URI;

public class ProblemHandlingClientHttpRequestFactory implements ClientHttpRequestFactory {
    private final ClientHttpRequestFactory delegate;

    public ProblemHandlingClientHttpRequestFactory(ClientHttpRequestFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        final ClientHttpRequest request = delegate.createRequest(uri, httpMethod);
        return new ProblemHandlingClientHttpRequest(request);
    }
}
