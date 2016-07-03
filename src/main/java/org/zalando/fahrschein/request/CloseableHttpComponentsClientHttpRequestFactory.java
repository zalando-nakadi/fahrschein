package org.zalando.fahrschein.request;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.io.IOException;
import java.net.URI;

public class CloseableHttpComponentsClientHttpRequestFactory implements ClientHttpRequestFactory {

    private final CloseableHttpClient client;

    public CloseableHttpComponentsClientHttpRequestFactory(final CloseableHttpClient client) {
        this.client = client;
    }

    @Override
    public ClientHttpRequest createRequest(final URI uri, final HttpMethod httpMethod) throws IOException {
        final HttpUriRequest request = createHttpUriRequest(httpMethod, uri);
        final HttpContext context = HttpClientContext.create();

        return new HttpComponentsClientHttpRequest(client, request, context);
    }

    protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
        switch (httpMethod) {
            case GET:
                return new HttpGet(uri);
            case POST:
                return new HttpPost(uri);
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
        }
    }
}
