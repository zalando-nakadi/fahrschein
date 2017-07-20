package org.zalando.fahrschein.http.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


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

}
