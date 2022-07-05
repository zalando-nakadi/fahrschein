package org.zalando.fahrschein.http.jdk11;

import org.junit.jupiter.api.Test;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Headers;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JavaNetHeadersDelegateTest {

    private HttpRequest.Builder requestBuilder() {
        return HttpRequest.newBuilder(URI.create("http://bla"));
    }

    @Test
    public void shouldBeCaseInsensitive() {
        final Headers headers = new JavaNetHeadersDelegate(HttpRequest.newBuilder()
                .uri(URI.create("http://bla"))
                .setHeader(Headers.CONTENT_TYPE, "text/plain").build().headers());

        assertEquals(singletonList("text/plain"), headers.get("content-type"));
        assertEquals("text/plain", headers.getFirst("CONTENT-TYPE"));
        assertEquals("text/plain", headers.getFirst("Content-Type"));
        assertEquals("text/plain", headers.getFirst("content-type"));
    }

    @Test
    public void shouldReturnHeaderNames() {
        final Headers headers = new JavaNetHeadersDelegate(requestBuilder()
                .setHeader(Headers.CONTENT_TYPE, "text/plain")
                .setHeader(Headers.CONTENT_ENCODING, "gzip").build().headers());

        assertEquals(new HashSet<>(asList("Content-Type", "Content-Encoding")), headers.headerNames());
    }

    @Test
    public void shouldReturnEmptySetOfHeadernames() {
        final Headers headers = new JavaNetHeadersDelegate(requestBuilder().build().headers());
        assertEquals(emptySet(), headers.headerNames());
    }

    @Test
    public void shouldReturnEmptyListForUnknownHeaders() {
        final Headers headers = new JavaNetHeadersDelegate(requestBuilder().build().headers());
        assertEquals(emptyList(), headers.get(Headers.CONTENT_TYPE));
    }

    @Test
    public void readOnlyViewShouldNotSupportAdd() {
        final Headers headers = new JavaNetHeadersDelegate(requestBuilder().build().headers());
        assertThrows(UnsupportedOperationException.class, () -> {
            headers.add(Headers.CONTENT_TYPE, "application/json");
        });
    }

    @Test
    public void readOnlyViewShouldNotSupportPut() {
        final Headers headers = new JavaNetHeadersDelegate(requestBuilder()
                .setHeader(Headers.CONTENT_TYPE, "text/plain")
                .setHeader(Headers.CONTENT_ENCODING, "gzip").build().headers());

        assertThrows(UnsupportedOperationException.class, () -> {
            headers.put(Headers.CONTENT_TYPE, "application/json");
        });
    }

    @Test
    public void shouldGetContentType() {
        final Headers headers = new JavaNetHeadersDelegate(requestBuilder()
                .setHeader(Headers.CONTENT_TYPE, "text/plain").build().headers());

        final ContentType contentType = headers.getContentType();
        assertNotNull(contentType);
        assertEquals(ContentType.TEXT_PLAIN_VALUE, contentType.getValue());
        assertEquals(ContentType.TEXT_PLAIN, contentType);
    }

    @Test
    public void shouldNotSetContentLength() {
        final Headers headers = new JavaNetHeadersDelegate(requestBuilder().build().headers());
        assertThrows(UnsupportedOperationException.class, () -> {
            headers.setContentLength(2000L);
        });
    }

    @Test
    public void shouldNotSetContentType() {
        final Headers headers = new JavaNetHeadersDelegate(requestBuilder().build().headers());
        assertThrows(UnsupportedOperationException.class, () -> {
            headers.setContentType(ContentType.TEXT_PLAIN);
        });
    }

    @Test
    public void shouldReturnMinusOneForUnknownContentLength() {
        final Headers headers = new JavaNetHeadersDelegate(requestBuilder().build().headers());
        assertEquals(-1L, headers.getContentLength());
    }
}
