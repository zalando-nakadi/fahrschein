package org.zalando.fahrschein.http.spring;

import org.springframework.http.client.ClientHttpRequest;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.GZIPOutputStream;

class RequestAdapter implements Request {
    private final ClientHttpRequest clientHttpRequest;
    private final boolean contentCompression;

    RequestAdapter(ClientHttpRequest clientHttpRequest, Boolean contentCompression) {
        this.clientHttpRequest = clientHttpRequest;
        this.contentCompression = contentCompression;

        if (contentCompression) {
            clientHttpRequest.getHeaders().set("Content-Encoding", "gzip");
        }
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
        if (this.contentCompression) {
            return new GZIPOutputStream(clientHttpRequest.getBody());
        }
        return clientHttpRequest.getBody();
    }

    @Override
    public Response execute() throws IOException {
        return new ResponseAdapter(clientHttpRequest.execute());
    }
}
