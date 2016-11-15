package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;

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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class ManagedCursorManagerTest {
    private MockRestServiceServer server;
    private ManagedCursorManager cursorManager;

    @Before
    public void foo() {
        final RestTemplate restTemplate = new RestTemplate();
        this.server = MockRestServiceServer.createServer(restTemplate);

        final ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.registerModules(new Jdk8Module(), new ParameterNamesModule(), new JavaTimeModule());

        this.cursorManager = new ManagedCursorManager(URI.create("http://example.com/"), requestFactory, objectMapper);
    }


    @Test
    public void shouldCommitCursor() throws IOException {
        server.expect(requestTo("http://example.com/subscriptions/1234/cursors"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Nakadi-StreamId", "stream-id"))
                .andExpect(jsonPath("$.items[0].partition", equalTo("0")))
                .andExpect(jsonPath("$.items[0].offset", equalTo("10")))
                .andExpect(jsonPath("$.items[0].cursor_token", equalTo("token")))
                .andRespond(withNoContent());

        final Subscription subscription = new Subscription("1234", "nakadi-client-test", Collections.singleton("foo"), "bar", OffsetDateTime.now());
        cursorManager.addSubscription(subscription);
        cursorManager.addStreamId(subscription, "stream-id");

        cursorManager.onSuccess("foo", new Cursor("0", "10", "foo", "token"));
    }

    @Test
    public void shouldGetCursors() throws IOException {
        server.expect(requestTo("http://example.com/subscriptions/1234/cursors"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"items\":[{\"partition\":\"0\",\"offset\":\"10\"},{\"partition\":\"1\",\"offset\":\"20\"}]}", MediaType.APPLICATION_JSON));

        final Subscription subscription = new Subscription("1234", "nakadi-client-test", Collections.singleton("foo"), "bar", OffsetDateTime.now());
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

    }

}
