package org.zalando.fahrschein.http.jdk11;

import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

public final class JavaNetRequestFactory implements RequestFactory {

    private final HttpClient client;
    private final Optional<Duration> requestTimeout;
    private final ContentEncoding contentEncoding;

    /**
     * @param client the HTTP client
     * @param requestTimeout (optional) the request timeout duration. See {@code java.net.http.HttpRequest.Builder#timeout}.
     * @param contentEncoding the encoding for publishing events
     */
    public JavaNetRequestFactory(HttpClient client, Optional<Duration> requestTimeout, ContentEncoding contentEncoding) {
        this.client = client;
        this.requestTimeout = requestTimeout;
        this.contentEncoding = contentEncoding;
    }

    @Override
    public Request createRequest(URI uri, String method) {
        return new JavaNetBufferingRequest(uri, method, client, requestTimeout, contentEncoding);
    }

}
