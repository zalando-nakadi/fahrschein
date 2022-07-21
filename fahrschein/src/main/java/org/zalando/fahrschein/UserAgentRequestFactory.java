package org.zalando.fahrschein;

import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.UserAgent;

import java.io.IOException;
import java.net.URI;

public final class UserAgentRequestFactory implements RequestFactory {
    private final RequestFactory delegate;
    private final String userAgent;

    UserAgentRequestFactory(final RequestFactory delegate) {
        this.userAgent = new UserAgent(delegate.getClass()).userAgent();
        this.delegate = delegate;
    }

    @Override
    public Request createRequest(URI uri, String method) throws IOException {
        final Request request = delegate.createRequest(uri, method);
        request.getHeaders().put(Headers.USER_AGENT, userAgent);
        return request;
    }
}
