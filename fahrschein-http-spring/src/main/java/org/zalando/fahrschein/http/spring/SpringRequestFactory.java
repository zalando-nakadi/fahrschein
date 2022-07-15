package org.zalando.fahrschein.http.spring;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.io.IOException;
import java.net.URI;

public final class SpringRequestFactory implements RequestFactory {
    private final ClientHttpRequestFactory clientRequestFactory;
    private final ContentEncoding contentEncoding;

    public SpringRequestFactory(ClientHttpRequestFactory clientRequestFactory, ContentEncoding contentEncoding) {
        this.clientRequestFactory = clientRequestFactory;
        this.contentEncoding = contentEncoding;
    }

    @Override
    public Request createRequest(URI uri, String method) throws IOException {
        return new SpringRequest(clientRequestFactory.createRequest(uri, HttpMethod.valueOf(method)), contentEncoding);
    }
}
