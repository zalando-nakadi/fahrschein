package org.zalando.fahrschein.http;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.AccessTokenProvider;

import java.io.IOException;
import java.net.URI;

import static java.util.Collections.singletonList;

public class AuthorizedClientHttpRequestFactory implements ClientHttpRequestFactory {
    private final ClientHttpRequestFactory delegate;
    private final AccessTokenProvider accessTokenProvider;

    public AuthorizedClientHttpRequestFactory(final ClientHttpRequestFactory delegate, final AccessTokenProvider accessTokenProvider) {
        this.delegate = delegate;
        this.accessTokenProvider = accessTokenProvider;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        final ClientHttpRequest request = delegate.createRequest(uri, httpMethod);
        request.getHeaders().put("Authorization", singletonList("Bearer ".concat(accessTokenProvider.getAccessToken())));
        return request;
    }
}
