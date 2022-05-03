package org.zalando.fahrschein.http.jdk11;

import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Headers;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Set;

final class JavaNetHeadersDelegate implements Headers {

    private static final String HEADER_IMPLEMENTATION_IS_READ_ONLY = "Header implementation is read-only";

    private final HttpHeaders headers;

    JavaNetHeadersDelegate(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public List<String> get(String headerName) {
        return headers.allValues(headerName);
    }

    @Override
    public void add(String headerName, String value) {
        throw new UnsupportedOperationException(HEADER_IMPLEMENTATION_IS_READ_ONLY);
    }

    @Override
    public void put(String headerName, String value) {
        throw new UnsupportedOperationException(HEADER_IMPLEMENTATION_IS_READ_ONLY);
    }

    @Override
    public String getFirst(String headerName) {
        return headers.firstValue(headerName).orElse(null);
    }

    @Override
    public Set<String> headerNames() {
        return headers.map().keySet();
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public void setContentLength(long contentLength) {
        throw new UnsupportedOperationException(HEADER_IMPLEMENTATION_IS_READ_ONLY);
    }

    @Override
    public ContentType getContentType() {
        return headers.firstValue(Headers.CONTENT_TYPE).map(v -> ContentType.valueOf(v)).orElse(null);
    }

    @Override
    public void setContentType(ContentType contentType) {
        throw new UnsupportedOperationException(HEADER_IMPLEMENTATION_IS_READ_ONLY);
    }
}
