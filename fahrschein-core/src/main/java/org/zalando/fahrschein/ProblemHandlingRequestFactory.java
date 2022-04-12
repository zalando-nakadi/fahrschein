package org.zalando.fahrschein;

import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.io.IOException;
import java.net.URI;

class ProblemHandlingRequestFactory implements RequestFactory {
    private final RequestFactory delegate;

    public ProblemHandlingRequestFactory(RequestFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public Request createRequest(URI uri, String method) throws IOException {
        final Request request = delegate.createRequest(uri, method);
        return new ProblemHandlingRequest(request);
    }
}
