package org.zalando.fahrschein.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;

class DelegatingClientHttpResponse implements ClientHttpResponse {
    private final ClientHttpResponse delegate;

    DelegatingClientHttpResponse(ClientHttpResponse delegate) {
        this.delegate = delegate;
    }

    public ClientHttpResponse getDelegate() {
        return delegate;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return delegate.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return delegate.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return delegate.getStatusText();
    }

    @Override
    public InputStream getBody() throws IOException {
        return delegate.getBody();
    }

    @Override
    public HttpHeaders getHeaders() {
        return delegate.getHeaders();
    }

    @Override
    public void close() {
        delegate.close();
    }
}
