package org.zalando.fahrschein;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.domain.SubscriptionRequest;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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

    private MockRestServiceServer server;
    private NakadiClient client;

    @Before
    public void setup() {
        final RestTemplate restTemplate = new RestTemplate();
        final MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        final ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
        final CursorManager cursorManager = mock(CursorManager.class);

        final NakadiClient nakadiClient = NakadiClient.builder(URI.create("http://example.com/"))
                .withClientHttpRequestFactory(requestFactory)
                .withCursorManager(cursorManager)
                .build();

        this.server = mockServer;
        this.client = nakadiClient;
    }

    @Test
    public void shouldGetPartitions() throws IOException {
        server.expect(requestTo("http://example.com/event-types/foobar/partitions"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"partition\":\"0\", \"oldest_available_offset\":\"10\", \"newest_available_offset\":\"20\"},{\"partition\":\"1\", \"oldest_available_offset\":\"BEGIN\",\"newest_available_offset\":\"10\"}]", MediaType.APPLICATION_JSON));

        final List<Partition> partitions = client.getPartitions("foobar");

        assertNotNull(partitions);
        assertThat(partitions, hasSize(2));
    }

    @Test
    public void shouldPostSubscription() throws IOException {
        server.expect(requestTo("http://example.com/subscriptions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.owning_application", equalTo("nakadi-client-test")))
                .andExpect(jsonPath("$.event_types[0]", equalTo("foo")))
                .andExpect(jsonPath("$.consumer_group", equalTo("bar")))
                .andExpect(jsonPath("$.read_from", equalTo("end")))
                .andRespond(withSuccess("{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo\"],\"consumer_group\":\"bar\",\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}", MediaType.APPLICATION_JSON));

        final Subscription subscription = client.subscribe("nakadi-client-test", "foo", "bar");

        assertNotNull(subscription);
        assertEquals("1234", subscription.getId());
        assertEquals("nakadi-client-test", subscription.getOwningApplication());
        assertEquals(Collections.singleton("foo"), subscription.getEventTypes());
        assertEquals("bar", subscription.getConsumerGroup());
        assertNotNull(subscription.getCreatedAt());
    }

    @Test
    public void shouldPostSubscriptionForMultipleEvents() throws IOException {
        server.expect(requestTo("http://example.com/subscriptions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.owning_application", equalTo("nakadi-client-test")))
                .andExpect(jsonPath("$.event_types[0]", equalTo("foo1")))
                .andExpect(jsonPath("$.event_types[1]", equalTo("foo2")))
                .andExpect(jsonPath("$.consumer_group", equalTo("bar")))
                .andExpect(jsonPath("$.read_from", equalTo("end")))
                .andRespond(withSuccess("{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo1\", \"foo2\"],\"consumer_group\":\"bar\",\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}", MediaType.APPLICATION_JSON));

        Set<String> eventNames = new HashSet<String>();
        eventNames.add("foo1");
        eventNames.add("foo2");

        final Subscription subscription = client.subscribe("nakadi-client-test", eventNames, "bar");

        assertNotNull(subscription);
        assertEquals("1234", subscription.getId());
        assertEquals("nakadi-client-test", subscription.getOwningApplication());
        assertEquals(2, subscription.getEventTypes().size());
        Set<String> expectedRows = new HashSet<String>();
        expectedRows.add("foo1");
        expectedRows.add("foo2");
        subscription.getEventTypes().stream().filter(eventType -> !expectedRows.contains(eventType)).forEach(eventType -> fail());
        assertEquals(2, subscription.getEventTypes().size());
        assertEquals("bar", subscription.getConsumerGroup());
        assertNotNull(subscription.getCreatedAt());
    }

    @Test
    public void shouldIncludeReadFromProperty() throws IOException {
        server.expect(requestTo("http://example.com/subscriptions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.owning_application", equalTo("nakadi-client-test")))
                .andExpect(jsonPath("$.event_types[0]", equalTo("foo")))
                .andExpect(jsonPath("$.consumer_group", equalTo("bar")))
                .andExpect(jsonPath("$.read_from", equalTo("begin")))
                .andRespond(withSuccess("{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo\"],\"consumer_group\":\"bar\",\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}", MediaType.APPLICATION_JSON));

        final Subscription subscription = client.subscribe("nakadi-client-test", "foo", "bar", SubscriptionRequest.Position.BEGIN);

        assertNotNull(subscription);
        assertEquals("1234", subscription.getId());
        assertEquals("nakadi-client-test", subscription.getOwningApplication());
        assertEquals(Collections.singleton("foo"), subscription.getEventTypes());
        assertEquals("bar", subscription.getConsumerGroup());
        assertNotNull(subscription.getCreatedAt());
    }

    @Test
    public void shouldPublishEvents() throws IOException {
        server.expect(requestTo("http://example.com/event-types/foobar/events"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$[0].id", equalTo("1")))
                .andExpect(jsonPath("$[1].id", equalTo("2")))
                .andRespond(withStatus(HttpStatus.OK));

        client.publish("foobar", asList(new SomeEvent("1"), new SomeEvent("2")));
    }

    @Test
    public void shouldHandleBatchItemResponseWhenPublishing() throws IOException {
        server.expect(requestTo("http://example.com/event-types/foobar/events"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$[0].id", equalTo("1")))
                .andExpect(jsonPath("$[1].id", equalTo("2")))
                .andRespond(withStatus(HttpStatus.MULTI_STATUS).contentType(MediaType.APPLICATION_JSON).body("[{\"publishing_status\":\"failed\",\"step\":\"validating\",\"detail\":\"baz\"}]"));

        expectedException.expect(EventPublishingException.class);
        expectedException.expectMessage("returned status [failed] in step [validating] with detail [baz]");

        client.publish("foobar", asList(new SomeEvent("1"), new SomeEvent("2")));
    }

}
