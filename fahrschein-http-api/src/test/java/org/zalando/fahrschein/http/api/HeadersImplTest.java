package org.zalando.fahrschein.http.api;

import org.junit.Test;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class HeadersImplTest {
    @Test
    public void shouldBeCaseInsensitive() {
        final HeadersImpl headers = new HeadersImpl();
        headers.put("Content-Type", "text/plain");

        assertEquals(singletonList("text/plain"), headers.get("content-type"));

        assertEquals("text/plain", headers.getFirst("CONTENT-TYPE"));
        assertEquals("text/plain", headers.getFirst("Content-Type"));
        assertEquals("text/plain", headers.getFirst("content-type"));
    }

    @Test
    public void shouldReturnHeaderNames() {
        final HeadersImpl headers = new HeadersImpl();
        headers.put("Content-Type", "text/plain");
        headers.put("Content-Length", "123");
        assertEquals(new HashSet<>(asList("Content-Type", "Content-Length")), headers.headerNames());
    }

}
