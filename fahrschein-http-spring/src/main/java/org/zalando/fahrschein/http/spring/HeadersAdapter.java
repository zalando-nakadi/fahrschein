package org.zalando.fahrschein.http.spring;

import org.springframework.http.HttpHeaders;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Headers;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class HeadersAdapter implements Headers {
    private final HttpHeaders headers;

    HeadersAdapter(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public List<String> get(String headerName) {
        return headers.get(headerName);
    }

    @Override
    public void add(String headerName, String value) {
        headers.add(headerName, value);
    }

    @Override
    public void put(String headerName, String value) {
        headers.put(headerName, Collections.singletonList(value));
    }

    @Nullable
    @Override
    public String getFirst(String headerName) {
        return headers.getFirst(headerName);
    }

    @Override
    public Set<String> headerNames() {
        return headers.keySet();
    }

    @Override
    public long getContentLength() {
        return headers.getContentLength();
    }

    @Override
    public void setContentLength(long contentLength) {
        headers.setContentLength(contentLength);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.valueOf(headers.getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Override
    public void setContentType(ContentType contentType) {
        headers.set(HttpHeaders.CONTENT_TYPE, contentType.getValue());
    }
}
