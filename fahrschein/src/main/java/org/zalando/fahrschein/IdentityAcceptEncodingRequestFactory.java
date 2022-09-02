package org.zalando.fahrschein;

import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.io.IOException;
import java.net.URI;

public class IdentityAcceptEncodingRequestFactory implements RequestFactory {

    private final RequestFactory delegate;

    public IdentityAcceptEncodingRequestFactory(RequestFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public Request createRequest(URI uri, String method) throws IOException {
        Request request = delegate.createRequest(uri, method);
        request.getHeaders().put(Headers.ACCEPT_ENCODING, "identity");
        return request;
    }
}
