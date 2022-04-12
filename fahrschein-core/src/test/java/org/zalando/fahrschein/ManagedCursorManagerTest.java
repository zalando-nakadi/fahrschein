package org.zalando.fahrschein;

import org.junit.Before;
import org.junit.Test;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.http.api.ContentType;

import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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
        server.expectRequestTo("http://example.com/subscriptions/1234/cursors", "POST")
                .andExpectHeader("X-Nakadi-StreamId", "stream-id")
                .andExpectJsonPath("$.items[0].partition", equalTo("0"))
                .andExpectJsonPath("$.items[0].offset", equalTo("10"))
                .andExpectJsonPath("$.items[0].cursor_token", equalTo("token"))
                .andRespondWith(204)
                .setup();

        final Subscription subscription = new Subscription("1234", "nakadi-client-test", Collections.singleton("foo"), "bar", OffsetDateTime.now(), null);
        cursorManager.addSubscription(subscription);
        cursorManager.addStreamId(subscription, "stream-id");

        cursorManager.onSuccess("foo", new Cursor("0", "10", "foo", "token"));

        server.verify();
    }

    @Test
    public void shouldThrowCursorCommitExceptionWhenServerReturnsError() throws IOException {
        int errorCode = 422;
        String streamId = "stream-id";
        server.expectRequestTo("http://example.com/subscriptions/1234/cursors", "POST")
                .andExpectHeader("X-Nakadi-StreamId", streamId)
                .andExpectJsonPath("$.items[0].partition", equalTo("0"))
                .andExpectJsonPath("$.items[0].offset", equalTo("10"))
                .andExpectJsonPath("$.items[0].cursor_token", equalTo("token"))
                .andRespondWith(errorCode, ContentType.TEXT_PLAIN, "Session with stream id " + streamId + " not found")
                .setup();

        final Subscription subscription = new Subscription("1234", "nakadi-client-test", Collections.singleton("foo"), "bar", OffsetDateTime.now(), null);
        cursorManager.addSubscription(subscription);
        cursorManager.addStreamId(subscription, streamId);
        Cursor cursor = new Cursor("0", "10", "foo", "token");
        try {
            cursorManager.onSuccess("foo", cursor);
            fail(CursorCommitException.class + " was not thrown");
        } catch (CursorCommitException e) {
            assertEquals(errorCode, e.getStatusCode());
            assertEquals(cursor, e.getCursor());
            assertEquals(subscription.getId(), e.getSubscriptionId());
            assertNotNull(e.getCause());
        } finally {
            server.verify();
        }
    }

    @Test
    public void shouldThrowCursorCommitExceptionWhenServerReturnsErrorUnknownCode() throws IOException {
        int errorCode = 201;
        String streamId = "stream-id";
        server.expectRequestTo("http://example.com/subscriptions/1234/cursors", "POST")
                .andExpectHeader("X-Nakadi-StreamId", streamId)
                .andRespondWith(errorCode, ContentType.TEXT_PLAIN, "Session with stream id " + streamId + " not found")
                .setup();

        final Subscription subscription = new Subscription("1234", "nakadi-client-test", Collections.singleton("foo"), "bar", OffsetDateTime.now(), null);
        cursorManager.addSubscription(subscription);
        cursorManager.addStreamId(subscription, streamId);
        Cursor cursor = new Cursor("0", "10", "foo", "token");
        try {
            cursorManager.onSuccess("foo", cursor);
            fail(CursorCommitException.class + " was not thrown");
        } catch (CursorCommitException e) {
            assertEquals(errorCode, e.getStatusCode());
            assertEquals(cursor, e.getCursor());
            assertEquals(subscription.getId(), e.getSubscriptionId());
            assertNull(e.getCause());
        } finally {
            server.verify();
        }
    }

    @Test
    public void shouldGetCursors() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions/1234/cursors", "GET")
                .andRespondWith(200, ContentType.APPLICATION_JSON, "{\"items\":[{\"partition\":\"0\",\"offset\":\"10\"},{\"partition\":\"1\",\"offset\":\"20\"}]}")
                .setup();

        final Subscription subscription = new Subscription("1234", "nakadi-client-test", Collections.singleton("foo"), "bar", OffsetDateTime.now(), null);
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
