package org.zalando.fahrschein;

import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.io.IOException;
import java.net.URI;

class AuthorizedRequestFactory implements RequestFactory {
    private final RequestFactory delegate;
    private final AccessTokenProvider accessTokenProvider;

    public AuthorizedRequestFactory(final RequestFactory delegate, final AccessTokenProvider accessTokenProvider) {
        this.delegate = delegate;
        this.accessTokenProvider = accessTokenProvider;
    }

    @Override
    public Request createRequest(URI uri, String method) throws IOException {
        final Request request = delegate.createRequest(uri, method);
        request.getHeaders().put(Headers.AUTHORIZATION, "Bearer ".concat(accessTokenProvider.getAccessToken()));
        return request;
    }
}
