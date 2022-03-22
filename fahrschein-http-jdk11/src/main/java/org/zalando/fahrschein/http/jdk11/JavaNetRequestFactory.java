package org.zalando.fahrschein.http.jdk11;

import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;

public final class JavaNetRequestFactory implements RequestFactory {

    private final HttpClient client;
    private final HttpRequestBuilderAdapter adapter;
    private final ContentEncoding contentEncoding;

    public JavaNetRequestFactory(HttpClient client, HttpRequestBuilderAdapter adapter, ContentEncoding contentEncoding) {
        this.client = client;
        this.adapter = adapter;
        this.contentEncoding = contentEncoding;
    }

    @Override
    public Request createRequest(URI uri, String method) throws IOException {
        return new JavaNetBufferingRequest(uri, method, client, adapter, contentEncoding);
    }

}
