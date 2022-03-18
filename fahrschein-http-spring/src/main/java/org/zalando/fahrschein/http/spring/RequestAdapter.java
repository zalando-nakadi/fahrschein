package org.zalando.fahrschein.http.spring;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

class RequestAdapter implements Request {
    private final ClientHttpRequest clientHttpRequest;
    private final boolean compressEntity;

    private static final List<HttpMethod> writeMethods = Arrays.asList(HttpMethod.POST, HttpMethod.PATCH, HttpMethod.PUT);

    RequestAdapter(ClientHttpRequest clientHttpRequest, Boolean contentCompression) {
        this.clientHttpRequest = clientHttpRequest;
        // only compress request if
        this.compressEntity = contentCompression &&
                !clientHttpRequest.getHeaders().containsKey("Content-Encoding") &&
                writeMethods.contains(clientHttpRequest.getMethod());
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
        if (compressEntity) {
            clientHttpRequest.getHeaders().set("Content-Encoding", "gzip");
            return new GZIPOutputStream(clientHttpRequest.getBody());
        }
        return clientHttpRequest.getBody();
    }

    @Override
    public Response execute() throws IOException {
        return new ResponseAdapter(clientHttpRequest.execute());
    }
}
