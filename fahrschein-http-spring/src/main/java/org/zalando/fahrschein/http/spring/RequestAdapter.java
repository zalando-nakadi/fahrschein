package org.zalando.fahrschein.http.spring;

import org.springframework.http.client.ClientHttpRequest;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

class RequestAdapter implements Request {
    private final ClientHttpRequest clientHttpRequest;

    RequestAdapter(ClientHttpRequest clientHttpRequest) {
        this.clientHttpRequest = clientHttpRequest;
    }

    @Override
    public String getMethod() {
        return clientHttpRequest.getMethod().name();
    }

    @Override
    public URI getURI() {
        return clientHttpRequest.getURI();
    }

    @Override
    public Headers getHeaders() {
        return new HeadersAdapter(clientHttpRequest.getHeaders());
    }

    @Override
    public OutputStream getBody() throws IOException {
        return clientHttpRequest.getBody();
    }

    @Override
    public Response execute() throws IOException {
        return new ResponseAdapter(clientHttpRequest.execute());
    }
}
