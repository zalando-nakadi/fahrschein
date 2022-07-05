package org.zalando.fahrschein.http.api;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ContentTypeTest {
    @Test
    public void shouldParseApplicationJson() {
        final ContentType contentType = ContentType.valueOf(ContentType.APPLICATION_JSON_VALUE);
        assertEquals("application/json", contentType.getValue());
        assertEquals("application", contentType.getType());
        assertEquals("json", contentType.getSubtype());
    }

    @Test
    public void shouldParseApplicationJsonWithCharset() {
        final ContentType contentType = ContentType.valueOf("application/json; charset=iso-8859-1");
        assertEquals("application/json; charset=iso-8859-1", contentType.getValue());
        assertEquals("application", contentType.getType());
        assertEquals("json", contentType.getSubtype());
    }

    @Test
    public void shouldParseProblemJson() {
        final ContentType contentType = ContentType.valueOf(ContentType.APPLICATION_PROBLEM_JSON_VALUE);
        assertEquals("application/problem+json", contentType.getValue());
        assertEquals("application", contentType.getType());
        assertEquals("problem+json", contentType.getSubtype());
    }

    @Test
    public void shouldFailOnIllegalContentType() {
        assertThrows(IllegalArgumentException.class, () -> {
            ContentType.valueOf("foo bar");
        });
    }

    @Test
    public void shouldBeHasheable() {
        final HashSet<ContentType> contentTypes = new HashSet<>();
        contentTypes.add(ContentType.APPLICATION_JSON);
        contentTypes.add(ContentType.TEXT_PLAIN);
        contentTypes.add(ContentType.valueOf("application/octet-stream"));

        assertEquals(3, contentTypes.size());

        assertTrue(contentTypes.contains(ContentType.valueOf("application/json")));
        assertTrue(contentTypes.contains(ContentType.valueOf("text/plain")));
        assertTrue(contentTypes.contains(ContentType.valueOf("application/octet-stream")));
    }

}
