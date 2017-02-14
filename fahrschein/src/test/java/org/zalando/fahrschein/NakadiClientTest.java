package org.zalando.fahrschein;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class NakadiClientTest {
    public static class SomeEvent {
        private final String id;

        public SomeEvent(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private MockServer server;
    private NakadiClient client;

    @Before
    public void setup() {
        final MockServer clientHttpRequestFactory = new MockServer();

        final CursorManager cursorManager = mock(CursorManager.class);

        final NakadiClient nakadiClient = NakadiClient.builder(URI.create("http://example.com/"))
                .withClientHttpRequestFactory(clientHttpRequestFactory)
                .withCursorManager(cursorManager)
                .build();

        this.server = clientHttpRequestFactory;
        this.client = nakadiClient;
    }

    @Test
    public void shouldGetPartitions() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/partitions", HttpMethod.GET)
                .andRespondWith(HttpStatus.OK, MediaType.APPLICATION_JSON, "[{\"partition\":\"0\", \"oldest_available_offset\":\"10\", \"newest_available_offset\":\"20\"},{\"partition\":\"1\", \"oldest_available_offset\":\"BEGIN\",\"newest_available_offset\":\"10\"}]")
        .setup();

        final List<Partition> partitions = client.getPartitions("foobar");

        assertNotNull(partitions);
        assertThat(partitions, hasSize(2));

        server.verify();
    }

    @Test
    public void shouldPostSubscription() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions", HttpMethod.POST)
                .andExpectJsonPath("$.owning_application", equalTo("nakadi-client-test"))
                .andExpectJsonPath("$.event_types[0]", equalTo("foo"))
                .andExpectJsonPath("$.consumer_group", equalTo("bar"))
                .andExpectJsonPath("$.read_from", equalTo("end"))
                .andRespondWith(HttpStatus.OK, MediaType.APPLICATION_JSON, "{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo\"],\"consumer_group\":\"bar\",\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}")
        .setup();

        final Subscription subscription = client.subscription("nakadi-client-test", "foo")
                .withConsumerGroup("bar")
                .subscribe();

        assertNotNull(subscription);
        assertEquals("1234", subscription.getId());
        assertEquals("nakadi-client-test", subscription.getOwningApplication());
        assertEquals(Collections.singleton("foo"), subscription.getEventTypes());
        assertEquals("bar", subscription.getConsumerGroup());
        assertNotNull(subscription.getCreatedAt());

        server.verify();
    }

    @Test
    public void shouldPostSubscriptionForMultipleEvents() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions", HttpMethod.POST)
                .andExpectJsonPath("$.owning_application", equalTo("nakadi-client-test"))
                .andExpectJsonPath("$.event_types[0]", equalTo("foo1"))
                .andExpectJsonPath("$.event_types[1]", equalTo("foo2"))
                .andExpectJsonPath("$.consumer_group", equalTo("bar"))
                .andExpectJsonPath("$.read_from", equalTo("end"))
                .andRespondWith(HttpStatus.OK, MediaType.APPLICATION_JSON, "{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo1\", \"foo2\"],\"consumer_group\":\"bar\",\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}")
                .setup();

        final Subscription subscription = client.subscription("nakadi-client-test", new LinkedHashSet<>(asList("foo1", "foo2")))
                .withConsumerGroup("bar")
                .subscribe();

        server.verify();

        assertNotNull(subscription);
        assertEquals("1234", subscription.getId());
        assertEquals("nakadi-client-test", subscription.getOwningApplication());
        assertEquals(2, subscription.getEventTypes().size());
        assertEquals(new HashSet<>(asList("foo1", "foo2")), subscription.getEventTypes());
        assertEquals("bar", subscription.getConsumerGroup());
        assertNotNull(subscription.getCreatedAt());
    }

    @Test
    public void shouldIncludeReadFromProperty() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions", HttpMethod.POST)
                .andExpectJsonPath("$.owning_application", equalTo("nakadi-client-test"))
                .andExpectJsonPath("$.event_types[0]", equalTo("foo"))
                .andExpectJsonPath("$.consumer_group", equalTo("bar"))
                .andExpectJsonPath("$.read_from", equalTo("begin"))
                .andRespondWith(HttpStatus.OK, MediaType.APPLICATION_JSON, "{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo\"],\"consumer_group\":\"bar\",\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}")
                .setup();

        final Subscription subscription = client.subscription("nakadi-client-test", "foo")
                .withConsumerGroup("bar")
                .readFromBegin()
                .subscribe();

        server.verify();

        assertNotNull(subscription);
        assertEquals("1234", subscription.getId());
        assertEquals("nakadi-client-test", subscription.getOwningApplication());
        assertEquals(Collections.singleton("foo"), subscription.getEventTypes());
        assertEquals("bar", subscription.getConsumerGroup());
        assertNotNull(subscription.getCreatedAt());
    }

    @Test
    public void shouldDeleteSubscription() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions/123", HttpMethod.DELETE)
                .andRespondWith(HttpStatus.NO_CONTENT).setup();

        client.deleteSubscription("123");

        server.verify();
    }

    @Test
    public void shouldThrowExceptionOnSubscriptionDeleteFailure() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions/123", HttpMethod.DELETE)
                .andRespondWith(HttpStatus.NOT_FOUND, MediaType.APPLICATION_JSON, "{\n" +
                        "  \"type\": \"http://httpstatus.es/404\",\n" +
                        "  \"title\": \"Not Found\",\n" +
                        "  \"status\": 404,\n" +
                        "  \"detail\": \"Subscription not found.\",\n" +
                        "  \"instance\": \"string\"\n" +
                        "}").setup();

        expectedException.expect(IOProblem.class);
        expectedException.expectMessage("Problem [http://httpstatus.es/404] with status [404]: [Not Found] [Subscription not found.]");

        client.deleteSubscription("123");

        server.verify();
    }

    @Test
    public void shouldPublishEvents() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/events", HttpMethod.POST)
                .andExpectJsonPath("$[0].id", equalTo("1"))
                .andExpectJsonPath("$[1].id", equalTo("2"))
                .andRespondWith(HttpStatus.OK)
                .setup();

        client.publish("foobar", asList(new SomeEvent("1"), new SomeEvent("2")));
    }

    @Test
    public void shouldHandleBatchItemResponseWhenPublishing() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/events", HttpMethod.POST)
                .andExpectJsonPath("$[0].id", equalTo("1"))
                .andExpectJsonPath("$[1].id", equalTo("2"))
                .andRespondWith(HttpStatus.MULTI_STATUS, MediaType.APPLICATION_JSON, "[{\"publishing_status\":\"failed\",\"step\":\"validating\",\"detail\":\"baz\"}]")
                .setup();

        expectedException.expect(EventPublishingException.class);
        expectedException.expectMessage("returned status [failed] in step [validating] with detail [baz]");

        client.publish("foobar", asList(new SomeEvent("1"), new SomeEvent("2")));

        server.verify();
    }

}
