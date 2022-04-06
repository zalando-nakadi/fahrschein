package org.zalando.fahrschein.http.jdk11;

import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public final class JavaNetRequestFactory implements RequestFactory {

    private final HttpClient client;
    private final Duration timeout;
    private final ContentEncoding contentEncoding;

    /**
     * @param client the HTTP client
     * @param timeout the timeout duration. See {@code java.net.http.HttpRequest.Builder#timeout}
     * @param contentEncoding the encoding for publishing events
     */
    public JavaNetRequestFactory(HttpClient client, Duration timeout, ContentEncoding contentEncoding) {
        this.client = client;
        this.timeout = timeout;
        this.contentEncoding = contentEncoding;
    }

    @Override
    public Request createRequest(URI uri, String method) {
        return new JavaNetBufferingRequest(uri, method, client, timeout, contentEncoding);
    }

}
