package org.zalando.fahrschein;

import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import java.io.IOException;
import java.net.URI;

class AuthorizedRequestFactory implements RequestFactory {
    private final RequestFactory delegate;
    private final AuthorizationProvider authorizationProvider;

    AuthorizedRequestFactory(final RequestFactory delegate, final AuthorizationProvider authorizationProvider) {
        this.delegate = delegate;
        this.authorizationProvider = authorizationProvider;
    }

    @Override
    public void disableContentCompression() {
        delegate.disableContentCompression();
    }

    @Override
    public Request createRequest(URI uri, String method) throws IOException {
        final Request request = delegate.createRequest(uri, method);
        request.getHeaders().put(Headers.AUTHORIZATION, authorizationProvider.getAuthorizationHeader());
        return request;
    }
}
