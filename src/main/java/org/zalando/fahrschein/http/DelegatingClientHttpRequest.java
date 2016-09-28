package org.zalando.fahrschein.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

class DelegatingClientHttpRequest implements ClientHttpRequest {

    private final ClientHttpRequest delegate;

    DelegatingClientHttpRequest(final ClientHttpRequest delegate) {
        this.delegate = delegate;
    }

    ClientHttpRequest getDelegate() {
        return delegate;
    }

    @Override
    public ClientHttpResponse execute() throws IOException {
        return delegate.execute();
    }

    @Override
    public OutputStream getBody() throws IOException {
        return delegate.getBody();
    }

    @Override
    public HttpMethod getMethod() {
        return delegate.getMethod();
    }

    @Override
    public URI getURI() {
        return delegate.getURI();
    }

    @Override
    public HttpHeaders getHeaders() {
        return delegate.getHeaders();
    }
}
