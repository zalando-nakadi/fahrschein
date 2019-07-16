package org.zalando.fahrschein;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.domain.SubscriptionRequest;
import org.zalando.fahrschein.http.api.ContentType;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.zalando.fahrschein.AuthorizationBuilder.authorization;
import static org.zalando.fahrschein.domain.Authorization.AuthorizationAttribute.ANYONE;

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
                .withRequestFactory(clientHttpRequestFactory)
                .withCursorManager(cursorManager)
                .build();

        this.server = clientHttpRequestFactory;
        this.client = nakadiClient;
    }

    @Test
    public void shouldGetPartitions() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/partitions", "GET")
                .andRespondWith(200, ContentType.APPLICATION_JSON, "[{\"partition\":\"0\", \"oldest_available_offset\":\"10\", \"newest_available_offset\":\"20\"},{\"partition\":\"1\", \"oldest_available_offset\":\"BEGIN\",\"newest_available_offset\":\"10\"}]")
        .setup();

        final List<Partition> partitions = client.getPartitions("foobar");

        assertNotNull(partitions);
        assertThat(partitions, hasSize(2));

        server.verify();
    }

    @Test
    public void shouldPostSubscription() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions", "POST")
                .andExpectJsonPath("$.owning_application", equalTo("nakadi-client-test"))
                .andExpectJsonPath("$.event_types[0]", equalTo("foo"))
                .andExpectJsonPath("$.consumer_group", equalTo("bar"))
                .andExpectJsonPath("$.read_from", equalTo("end"))
                .andRespondWith(200, ContentType.APPLICATION_JSON, "{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo\"],\"consumer_group\":\"bar\",\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}")
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
        server.expectRequestTo("http://example.com/subscriptions", "POST")
                .andExpectJsonPath("$.owning_application", equalTo("nakadi-client-test"))
                .andExpectJsonPath("$.event_types[0]", equalTo("foo1"))
                .andExpectJsonPath("$.event_types[1]", equalTo("foo2"))
                .andExpectJsonPath("$.consumer_group", equalTo("bar"))
                .andExpectJsonPath("$.read_from", equalTo("end"))
                .andRespondWith(200, ContentType.APPLICATION_JSON, "{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo1\", \"foo2\"],\"consumer_group\":\"bar\",\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}")
                .setup();

        final Subscription subscription = client.subscription("nakadi-client-test", new HashSet<>(asList("foo1", "foo2")))
                .withConsumerGroup("bar")
                .subscribe();

        server.verify();

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
        server.expectRequestTo("http://example.com/subscriptions", "POST")
                .andExpectJsonPath("$.owning_application", equalTo("nakadi-client-test"))
                .andExpectJsonPath("$.event_types[0]", equalTo("foo"))
                .andExpectJsonPath("$.consumer_group", equalTo("bar"))
                .andExpectJsonPath("$.read_from", equalTo("begin"))
                .andRespondWith(200, ContentType.APPLICATION_JSON, "{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo\"],\"consumer_group\":\"bar\",\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}")
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
    public void shouldIncludeAuthorization() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions", "POST")
                .andExpectJsonPath("$.authorization", notNullValue())
                .andExpectJsonPath("$.authorization.admins[0].data_type", equalTo("user"))
                .andExpectJsonPath("$.authorization.admins[0].value", equalTo("mmusterman"))
                .andExpectJsonPath("$.authorization.admins[1].data_type", equalTo("service"))
                .andExpectJsonPath("$.authorization.admins[1].value", equalTo("jdoe"))
                .andExpectJsonPath("$.authorization.readers.[0].data_type", equalTo("*"))
                .andExpectJsonPath("$.authorization.readers.[0].value", equalTo("*"))
                .andRespondWith(200, ContentType.APPLICATION_JSON, "{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo\"],\"consumer_group\":\"default\",\"authorization\":{\"admins\":[{\"data_type\":\"user\",\"value\":\"mmusterman\"},{\"data_type\":\"service\",\"value\":\"jdoe\"}],\"readers\":[{\"data_type\":\"*\",\"value\":\"*\"}]},\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}")
                .setup();

        final Subscription subscription = client.subscription("nakadi-client-test", "foo")
                .withAuthorization(authorization()
                        .addAdmin("user", "mmusterman")
                        .addAdmin("service", "jdoe")
                        .withReaders(ANYONE)
                        .build())
                .subscribe();

        server.verify();

        assertNotNull(subscription);
        assertEquals("user", subscription.getAuthorization().getAdmins().get(0).getDataType());
        assertEquals("mmusterman", subscription.getAuthorization().getAdmins().get(0).getValue());
        assertEquals("service", subscription.getAuthorization().getAdmins().get(1).getDataType());
        assertEquals("jdoe", subscription.getAuthorization().getAdmins().get(1).getValue());
        assertEquals("*", subscription.getAuthorization().getReaders().get(0).getDataType());
        assertEquals("*", subscription.getAuthorization().getReaders().get(0).getValue());
    }

    @Test
    public void shouldDeleteSubscription() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions/123", "DELETE")
                .andRespondWith(204).setup();

        client.deleteSubscription("123");

        server.verify();
    }

    @Test
    public void shouldThrowExceptionOnSubscriptionDeleteFailure() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions/123", "DELETE")
                .andRespondWith(404, ContentType.APPLICATION_JSON, "{\n" +
                        "  \"type\": \"http://httpstatus.es/404\",\n" +
                        "  \"title\": \"Not Found\",\n" +
                        "  \"status\": 404,\n" +
                        "  \"detail\": \"Subscription not found.\"\n" +
                        "}").setup();

        expectedException.expect(IOProblem.class);
        expectedException.expectMessage("Problem [http://httpstatus.es/404] with status [404]: [Not Found] [Subscription not found.]");

        client.deleteSubscription("123");

        server.verify();
    }

    @Test
    public void shouldThrowExceptionOnSubscriptionToUnknownEvent() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions", "POST")
                .andRespondWith(422, ContentType.APPLICATION_JSON, "{\n" +
                        "  \"type\": \"http://httpstatus.es/422\",\n" +
                        "  \"title\": \"Unprocessable Entity\",\n" +
                        "  \"status\": 422,\n" +
                        "  \"detail\": \"Eventtype does not exist.\"\n" +
                        "}").setup();

        expectedException.expect(IOProblem.class);
        expectedException.expectMessage("Problem [http://httpstatus.es/422] with status [422]: [Unprocessable Entity] [Eventtype does not exist.]");

        client.subscribe("nakadi-client-test", Collections.singleton("non-existing-event"), "nakadi-client-test-consumer", SubscriptionRequest.Position.BEGIN, null, null);

        server.verify();
    }

    @Test
    public void shouldPublishEvents() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/events", "POST")
                .andExpectJsonPath("$[0].id", equalTo("1"))
                .andExpectJsonPath("$[1].id", equalTo("2"))
                .andRespondWith(200)
                .setup();

        client.publish("foobar", asList(new SomeEvent("1"), new SomeEvent("2")));
    }

    @Test
    public void shouldHandleMultiStatusWhenPublishing() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/events", "POST")
                .andExpectJsonPath("$[0].id", equalTo("1"))
                .andExpectJsonPath("$[1].id", equalTo("2"))
                .andRespondWith(207, ContentType.APPLICATION_JSON, "[{\"publishing_status\":\"failed\",\"step\":\"validating\",\"detail\":\"baz\"}]")
                .setup();

        expectedException.expect(EventPublishingException.class);
        expectedException.expectMessage("returned status [failed] in step [validating] with detail [baz]");

        client.publish("foobar", asList(new SomeEvent("1"), new SomeEvent("2")));

        server.verify();
    }

    @Test
    public void shouldHandleSuccessFulMultiStatusWhenPublishing() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/events", "POST")
                .andExpectJsonPath("$[0].id", equalTo("1"))
                .andExpectJsonPath("$[1].id", equalTo("2"))
                .andRespondWith(207, ContentType.APPLICATION_JSON, "[{\"publishing_status\":\"submitted\"}, {\"publishing_status\":\"submitted\"}]")
                .setup();

        client.publish("foobar", asList(new SomeEvent("1"), new SomeEvent("2")));

        server.verify();
    }

    @Test
    public void shouldHandleBatchItemResponseWhenPublishing() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/events", "POST")
                .andExpectJsonPath("$[0].id", equalTo("1"))
                .andExpectJsonPath("$[1].id", equalTo("2"))
                .andRespondWith(422, ContentType.APPLICATION_JSON, "[{\"publishing_status\":\"aborted\",\"step\":\"publishing\",\"detail\":\"baz\"}]")
                .setup();

        expectedException.expect(EventPublishingException.class);
        expectedException.expectMessage("returned status [aborted] in step [publishing] with detail [baz]");

        client.publish("foobar", asList(new SomeEvent("1"), new SomeEvent("2")));

        server.verify();
    }

}
