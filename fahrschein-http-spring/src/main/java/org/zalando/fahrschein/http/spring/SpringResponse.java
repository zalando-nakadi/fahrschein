package org.zalando.fahrschein.http.spring;

import org.springframework.http.client.ClientHttpResponse;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.Response;

import java.io.IOException;
import java.io.InputStream;

class SpringResponse implements Response {
    private final ClientHttpResponse clientHttpResponse;

    SpringResponse(ClientHttpResponse clientHttpResponse) {
        this.clientHttpResponse = clientHttpResponse;
    }

    @Override
    public int getStatusCode() throws IOException {
        return clientHttpResponse.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return clientHttpResponse.getStatusText();
    }

    @Override
    public Headers getHeaders() {
        return new HeadersAdapter(clientHttpResponse.getHeaders());
    }

    @Override
    public InputStream getBody() throws IOException {
        return clientHttpResponse.getBody();
    }

    @Override
    public void close() {
        clientHttpResponse.close();
    }
}
