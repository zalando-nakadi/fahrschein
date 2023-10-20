package org.zalando.fahrschein;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.zalando.fahrschein.domain.BatchItemResponse;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.domain.SubscriptionRequest;
import org.zalando.fahrschein.http.api.ContentType;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.zalando.fahrschein.AuthorizationBuilder.authorization;

public class NakadiClientTest {
    private CursorManager cursorManager;

    private MockServer server;
    private NakadiClient client;

    @BeforeEach
    public void setup() {
        final MockServer clientHttpRequestFactory = new MockServer();

        cursorManager = mock(CursorManager.class);

        final NakadiClient nakadiClient = NakadiClient.builder(URI.create("http://example.com/"), clientHttpRequestFactory)
                .withCursorManager(cursorManager)
                .withPublishingRetryStrategyAndBackoff(PublishingRetryStrategy.NONE, new NoBackoffStrategy())
                .build();

        this.server = clientHttpRequestFactory;
        this.client = nakadiClient;
    }

    @Test
    public void shouldAddUserAgent() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/partitions", "GET")
                .andExpectHeader("User-Agent", Matchers.startsWith("Fahrschein/"))
                .andRespondWith(200, ContentType.APPLICATION_JSON, "[]")
                .setup();

        client.getPartitions("foobar");
        server.verify();
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
                .andExpectJsonPath("$.authorization.admins.size()", equalTo(2))
                .andExpectJsonPath("$.authorization.admins[0].data_type", equalTo("user"))
                .andExpectJsonPath("$.authorization.admins[0].value", equalTo("mmusterman"))
                .andExpectJsonPath("$.authorization.admins[1].data_type", equalTo("service"))
                .andExpectJsonPath("$.authorization.admins[1].value", equalTo("jdoe"))
                .andExpectJsonPath("$.authorization.readers.size()", equalTo(1))
                .andExpectJsonPath("$.authorization.readers.[0].data_type", equalTo("rick"))
                .andExpectJsonPath("$.authorization.readers.[0].value", equalTo("astley"))
                .andRespondWith(200, ContentType.APPLICATION_JSON, "{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo\"],\"consumer_group\":\"default\",\"authorization\":{\"admins\":[{\"data_type\":\"user\",\"value\":\"mmusterman\"},{\"data_type\":\"service\",\"value\":\"jdoe\"}],\"readers\":[{\"data_type\":\"rick\",\"value\":\"astley\"}]},\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}")
                .setup();

        final Subscription subscription = client.subscription("nakadi-client-test", "foo")
                .withAuthorization(authorization()
                        .addAdmin("user", "mmusterman")
                        .addAdmin("service", "jdoe")
                        .addReader("rick", "astley")
                        .build())
                .subscribe();

        server.verify();

        assertNotNull(subscription);
        assertEquals("user", subscription.getAuthorization().getAdmins().get(0).getDataType());
        assertEquals("mmusterman", subscription.getAuthorization().getAdmins().get(0).getValue());
        assertEquals("service", subscription.getAuthorization().getAdmins().get(1).getDataType());
        assertEquals("jdoe", subscription.getAuthorization().getAdmins().get(1).getValue());
        assertEquals("rick", subscription.getAuthorization().getReaders().get(0).getDataType());
        assertEquals("astley", subscription.getAuthorization().getReaders().get(0).getValue());
    }

    @Test
    public void shouldIncludeAllowAllAuthorizationByDefault() throws IOException {
        server.expectRequestTo("http://example.com/subscriptions", "POST")
                .andExpectJsonPath("$.authorization", notNullValue())
                .andExpectJsonPath("$.authorization.admins.size()", equalTo(1))
                .andExpectJsonPath("$.authorization.admins[0].data_type", equalTo("*"))
                .andExpectJsonPath("$.authorization.admins[0].value", equalTo("*"))
                .andExpectJsonPath("$.authorization.readers.size()", equalTo(1))
                .andExpectJsonPath("$.authorization.readers.[0].data_type", equalTo("*"))
                .andExpectJsonPath("$.authorization.readers.[0].value", equalTo("*"))
                .andRespondWith(200, ContentType.APPLICATION_JSON, "{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo\"],\"consumer_group\":\"default\",\"authorization\":{\"admins\":[{\"data_type\":\"*\",\"value\":\"*\"}],\"readers\":[{\"data_type\":\"*\",\"value\":\"*\"}]},\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}")
                .setup();

        final Subscription subscription = client.subscription("nakadi-client-test", "foo")
                .withAuthorization(authorization().build())
                .subscribe();

        server.verify();

        assertNotNull(subscription);
        assertEquals("*", subscription.getAuthorization().getAdmins().get(0).getDataType());
        assertEquals("*", subscription.getAuthorization().getAdmins().get(0).getValue());
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



        IOProblem expectedException = assertThrows(IOProblem.class, () -> {
            client.deleteSubscription("123");
        });
        server.verify();

        assertEquals("Problem [http://httpstatus.es/404] with status [404]: [Not Found] [Subscription not found.]", expectedException.getMessage());
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



        IOProblem expectedException = assertThrows(IOProblem.class, () -> {
            client.subscribe("nakadi-client-test", Collections.singleton("non-existing-event"), "nakadi-client-test-consumer", SubscriptionRequest.Position.BEGIN, null, null);
        });
        server.verify();
        assertEquals("Problem [http://httpstatus.es/422] with status [422]: [Unprocessable Entity] [Eventtype does not exist.]", expectedException.getMessage());
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
    public void shouldPublishEventsWithRequestHandler() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/events", "POST")
                .andExpectJsonPath("$[0].id", equalTo("1"))
                .andExpectJsonPath("$[1].id", equalTo("2"))
                .andRespondWith(200)
                .setup();

        EventPublishingHandler eventPublishingHandler = mock(EventPublishingHandler.class);

        client = NakadiClient.builder(URI.create("http://example.com/"), server)
                .withCursorManager(this.cursorManager)
                .withRequestHandler(eventPublishingHandler)
                .build();

        String eventName = "foobar";
        List<SomeEvent> someEvents = asList(new SomeEvent("1"), new SomeEvent("2"));

        client.publish("foobar", someEvents);

        server.verify();
        verify(eventPublishingHandler, Mockito.atMostOnce()).onPublish(eq(eventName), eq(someEvents));
        verify(eventPublishingHandler, Mockito.atMostOnce()).afterPublish();
    }


    @Test
    public void shouldPublishEventsWithRequestHandlerAndFailWithUnprocessableEntity() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/events", "POST")
                .andExpectJsonPath("$[0].id", equalTo("1"))
                .andExpectJsonPath("$[1].id", equalTo("2"))
                .andRespondWith(422, ContentType.APPLICATION_JSON, "[{\"eid\":\"event-one\",\"publishing_status\":\"failed\",\"step\":\"validating\",\"detail\":\"baz\"}]")
                .setup();

        EventPublishingHandler eventPublishingHandler = mock(EventPublishingHandler.class);

        client = NakadiClient.builder(URI.create("http://example.com/"), server)
                .withCursorManager(this.cursorManager)
                .withRequestHandler(eventPublishingHandler)
                .build();

        String eventName = "foobar";
        List<SomeEvent> someEvents = asList(new SomeEvent("1"), new SomeEvent("2"));

        EventValidationException expectedException = assertThrows(EventValidationException.class, () -> {
            client.publish("foobar", someEvents);
        });

        server.verify();
        assertEquals("Event publishing of [event-one] returned status [failed] in step [validating] with detail [baz]", expectedException.getMessage());
        verify(eventPublishingHandler, Mockito.atMostOnce()).onPublish(eq(eventName), eq(someEvents));
        verify(eventPublishingHandler, Mockito.atMostOnce()).onError(eq(someEvents), eq(expectedException));
    }

    @Test
    public void shouldPublishEventsWithRequestHandlerAndFailPartially() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/events", "POST")
                .andExpectJsonPath("$[0].id", equalTo("1"))
                .andExpectJsonPath("$[1].id", equalTo("2"))
                .andRespondWith(207, ContentType.APPLICATION_JSON,
                        "[{\"eid\":\"event-one\",\"publishing_status\":\"failed\",\"step\":\"publishing\",\"detail\":\"baz\"},"
                 + "{\"eid\":\"event-two\",\"publishing_status\":\"submitted\",\"step\":\"publishing\",\"detail\":\"\"}]")
                .setup();

        EventPublishingHandler eventPublishingHandler = mock(EventPublishingHandler.class);

        client = NakadiClient.builder(URI.create("http://example.com/"), server)
                .withCursorManager(this.cursorManager)
                .withRequestHandler(eventPublishingHandler)
                .withPublishingRetryStrategyAndBackoff(PublishingRetryStrategy.NONE, new NoBackoffStrategy())
                .build();

        String eventName = "foobar";
        List<SomeEvent> someEvents = asList(new SomeEvent("1"), new SomeEvent("2"));

        EventPersistenceException expectedException = assertThrows(EventPersistenceException.class, () -> {
            client.publish("foobar", someEvents);
        });

        server.verify();
        assertEquals(2, expectedException.getResponses().length);
        assertEquals(expectedException.getResponses()[0].getPublishingStatus(), BatchItemResponse.PublishingStatus.FAILED);
        assertEquals(expectedException.getResponses()[1].getPublishingStatus(), BatchItemResponse.PublishingStatus.SUBMITTED);
        verify(eventPublishingHandler, Mockito.atMostOnce()).onPublish(eq(eventName), eq(someEvents));
        verify(eventPublishingHandler, Mockito.atMostOnce()).onError(eq(someEvents), eq(expectedException));
    }

    @Test
    public void shouldHandleMultiStatusWhenPublishing() throws IOException {
        server.expectRequestTo("http://example.com/event-types/foobar/events", "POST")
                .andExpectJsonPath("$[0].id", equalTo("1"))
                .andExpectJsonPath("$[1].id", equalTo("2"))
                .andRespondWith(207, ContentType.APPLICATION_JSON, "[{\"eid\":\"event-one\",\"publishing_status\":\"failed\",\"step\":\"validating\",\"detail\":\"baz\"}]")
                .setup();

        EventPersistenceException expectedException = assertThrows(EventPersistenceException.class, () -> {
            client.publish("foobar", asList(new SomeEvent("1"), new SomeEvent("2")));
        });
        server.verify();

        assertEquals("Event publishing of [event-one] returned status [failed] in step [validating] with detail [baz]", expectedException.getMessage());
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
                .andRespondWith(422, ContentType.APPLICATION_JSON, "[{\"eid\":\"some-event\",\"publishing_status\":\"aborted\",\"step\":\"publishing\",\"detail\":\"baz\"}]")
                .setup();


        Throwable expectedException = assertThrows(EventValidationException.class, () -> {
            client.publish("foobar", asList(new SomeEvent("1"), new SomeEvent("2")));
        });

        server.verify();
        assertEquals("Event publishing of [some-event] returned status [aborted] in step [publishing] with detail [baz]", expectedException.getMessage());
    }

    public static class SomeEvent {
        private final String id;

        public SomeEvent(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
