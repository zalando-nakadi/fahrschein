package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ForwardingObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.io.IOException;
import java.net.URI;

public class ProblemHandlingClientHttpRequestFactory implements DelegatingClientHttpRequestFactory {
    private final ClientHttpRequestFactory delegate;
    private final ObjectMapper objectMapper;

    public ProblemHandlingClientHttpRequestFactory(ClientHttpRequestFactory delegate, ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;
    }

    @Override
    public ClientHttpRequestFactory delegate() {
        return delegate;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        final ClientHttpRequest request = delegate.createRequest(uri, httpMethod);
        return new ProblemHandlingClientHttpRequest(request, objectMapper);
    }
}
