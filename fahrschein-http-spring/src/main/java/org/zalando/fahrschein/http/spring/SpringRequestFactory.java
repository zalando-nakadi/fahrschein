package org.zalando.fahrschein.http.spring;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.io.IOException;
import java.net.URI;

public class SpringRequestFactory implements RequestFactory {
    private final ClientHttpRequestFactory clientRequestFactory;
    private Boolean contentCompression;

    public SpringRequestFactory(ClientHttpRequestFactory clientRequestFactory) {
        this(clientRequestFactory, true);
    }

    public SpringRequestFactory(ClientHttpRequestFactory clientRequestFactory, Boolean contentCompression) {
        this.clientRequestFactory = clientRequestFactory;
        this.contentCompression = contentCompression;
    }

    @Override
    public void disableContentCompression() {
        this.contentCompression = false;
    }

    @Override
    public Request createRequest(URI uri, String method) throws IOException {
        return new RequestAdapter(clientRequestFactory.createRequest(uri, HttpMethod.valueOf(method)), contentCompression);
    }
}
