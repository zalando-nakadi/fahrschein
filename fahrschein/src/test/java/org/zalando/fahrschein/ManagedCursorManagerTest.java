package org.zalando.fahrschein;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

public class ManagedCursorManagerTest {
    private MockServer server;
    private ManagedCursorManager cursorManager;

    @Before
    public void foo() {
        this.server = new MockServer();
        this.cursorManager = new ManagedCursorManager(URI.create("http://example.com/"), this.server);
    }

    @Test
    public void shouldCommitCursor() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions/1234/cursors", HttpMethod.POST)
                .andExpectHeader("X-Nakadi-StreamId", "stream-id")
                .andExpectJsonPath("$.items[0].partition", equalTo("0"))
                .andExpectJsonPath("$.items[0].offset", equalTo("10"))
                .andExpectJsonPath("$.items[0].cursor_token", equalTo("token"))
                .andRespondWith(HttpStatus.NO_CONTENT)
                .setup();

        final Subscription subscription = new Subscription("1234", "nakadi-client-test", Collections.singleton("foo"), "bar", new Date());
        cursorManager.addSubscription(subscription);
        cursorManager.addStreamId(subscription, "stream-id");

        cursorManager.onSuccess("foo", new Cursor("0", "10", "foo", "token"));

        server.verify();
    }

    @Test
    public void shouldGetCursors() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions/1234/cursors", HttpMethod.GET)
                .andRespondWith(HttpStatus.OK, MediaType.APPLICATION_JSON, "{\"items\":[{\"partition\":\"0\",\"offset\":\"10\"},{\"partition\":\"1\",\"offset\":\"20\"}]}")
                .setup();

        final Subscription subscription = new Subscription("1234", "nakadi-client-test", Collections.singleton("foo"), "bar", new Date());
        cursorManager.addSubscription(subscription);

        final Collection<Cursor> cursors = cursorManager.getCursors("foo");
        assertThat(cursors, hasSize(2));
        final Iterator<Cursor> it = cursors.iterator();
        {
            final Cursor cursor1 = it.next();
            assertEquals("0", cursor1.getPartition());
            assertEquals("10", cursor1.getOffset());
        }
        {
            final Cursor cursor2 = it.next();
            assertEquals("1", cursor2.getPartition());
            assertEquals("20", cursor2.getOffset());
        }

        server.verify();
    }

}
