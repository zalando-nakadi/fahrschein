package org.zalando.fahrschein;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpRetryException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.zalando.fahrschein.domain.Event;
import org.zalando.fahrschein.domain.Metadata;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.simple.SimpleRequestFactory;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonPathBody.jsonPath;
import static org.mockserver.model.MediaType.APPLICATION_JSON;
import static org.mockserver.verify.VerificationTimes.exactly;
import static org.zalando.fahrschein.ExponentialBackoffStrategy.DEFAULT_BACKOFF_FACTOR;
import static org.zalando.fahrschein.ExponentialBackoffStrategy.DEFAULT_INITIAL_DELAY;
import static org.zalando.fahrschein.ExponentialBackoffStrategy.DEFAULT_MAX_DELAY;

class NakadiClientIntegrationTest {

    private ClientAndServer clientAndServer;

    private NakadiClient client;

    @BeforeEach
    void setUp() {
        clientAndServer = ClientAndServer.startClientAndServer(1080);
        client = NakadiClient.builder(URI.create("http://localhost:1080/"),
                        new ProblemHandlingRequestFactory(new SimpleRequestFactory(ContentEncoding.GZIP)))
                .withCursorManager(mock(CursorManager.class))
                .withPublishingRetryStrategyAndBackoff(PublishingRetryStrategy.PARTIAL, new ExponentialBackoffStrategy(
                        DEFAULT_INITIAL_DELAY,
                        DEFAULT_BACKOFF_FACTOR,
                        DEFAULT_MAX_DELAY,
                        3
                ))
                .build();
    }

    @AfterEach
    void tearDown() throws ExecutionException, InterruptedException, TimeoutException {
        clientAndServer.stopAsync().get(30, TimeUnit.SECONDS);
    }

    @Test
    public void shouldRetryPublishingPartialBatchWithBackoff() throws IOException, BackoffException {
        clientAndServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/event-types/foobar/events")
                                .withBody(jsonPath("$[*].metadata[?(@.size() == 3)]"))
                                .withBody(jsonPath("$[0].metadata[?(@.eid == 'eid1')]"))
                                .withBody(jsonPath("$[1].metadata[?(@.eid == 'eid2')]"))
                                .withBody(jsonPath("$[2].metadata[?(@.eid == 'eid3')]"))

                ).respond(
                        response()
                                .withStatusCode(207)
                                .withContentType(APPLICATION_JSON)
                                .withBody("[\n" +
                                        "  {\n" +
                                        "    \"eid\": \"eid1\",\n" +
                                        "    \"publishing_status\": \"aborted\",\n" +
                                        "    \"step\": \"publishing\",\n" +
                                        "    \"detail\": \"baz\"\n" +
                                        "  },\n" +
                                        "\n" +
                                        "  {\n" +
                                        "    \"eid\": \"eid2\",\n" +
                                        "    \"publishing_status\": \"failed\",\n" +
                                        "    \"step\": \"publishing\",\n" +
                                        "    \"detail\": \"baz\"\n" +
                                        "  },\n" +
                                        "  {\n" +
                                        "    \"eid\": \"eid3\",\n" +
                                        "    \"publishing_status\": \"submitted\",\n" +
                                        "    \"step\": \"validating\",\n" +
                                        "    \"detail\": \"baz\"\n" +
                                        "  }\n" +
                                        "]")


                );


        clientAndServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/event-types/foobar/events")
                                .withBody(jsonPath("$[*].metadata[?(@.size() == 2)]"))
                                .withBody(jsonPath("$[0].metadata[?(@.eid == 'eid1')]"))
                                .withBody(jsonPath("$[1].metadata[?(@.eid == 'eid2')]"))

                ).respond(
                        response()
                                .withStatusCode(207)
                                .withContentType(APPLICATION_JSON)
                                .withBody("[\n" +
                                        "  {\n" +
                                        "    \"eid\": \"eid1\",\n" +
                                        "    \"publishing_status\": \"aborted\",\n" +
                                        "    \"step\": \"publishing\",\n" +
                                        "    \"detail\": \"baz\"\n" +
                                        "  },\n" +
                                        "\n" +
                                        "  {\n" +
                                        "    \"eid\": \"eid2\",\n" +
                                        "    \"publishing_status\": \"submitted\",\n" +
                                        "    \"step\": \"publishing\",\n" +
                                        "    \"detail\": \"baz\"\n" +
                                        "  }" +
                                        "\n" +
                                        "]")


                );

        clientAndServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/event-types/foobar/events")
                                .withBody(jsonPath("$[*].metadata[?(@.size() == 1)]"))
                                .withBody(jsonPath("$[0].metadata[?(@.eid == 'eid1')]"))
                ).respond(
                        response()
                                .withStatusCode(200)
                                .withContentType(APPLICATION_JSON)
                                .withBody("[\n" +
                                        "  {\n" +
                                        "    \"eid\": \"eid1\",\n" +
                                        "    \"publishing_status\": \"submitted\",\n" +
                                        "    \"step\": \"publishing\",\n" +
                                        "    \"detail\": \"baz\"\n" +
                                        "  }" +
                                        "\n" +
                                        "]")


                );



        client.publish("foobar", asList(
                        new SomeEvent("eid1", new Metadata("eid1", OffsetDateTime.now())),
                        new SomeEvent("eid2", new Metadata("eid2", OffsetDateTime.now())),
                        new SomeEvent("eid3", new Metadata("eid3", OffsetDateTime.now())))
        );

        clientAndServer.verify(request().withPath("/event-types/foobar/events"), exactly(3));
    }

    @Test
    public void shouldRetryPublishingFullBatchWithBackoff() {
        clientAndServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/event-types/foobar/events")
                                .withBody(jsonPath("$[*].metadata[?(@.size() == 3)]"))
                                .withBody(jsonPath("$[0].metadata[?(@.eid == 'eid1')]"))
                                .withBody(jsonPath("$[1].metadata[?(@.eid == 'eid2')]"))
                                .withBody(jsonPath("$[2].metadata[?(@.eid == 'eid3')]"))

                ).respond(
                        response()
                                .withStatusCode(207)
                                .withContentType(APPLICATION_JSON)
                                .withBody("[\n" +
                                        "  {\n" +
                                        "    \"eid\": \"eid1\",\n" +
                                        "    \"publishing_status\": \"aborted\",\n" +
                                        "    \"step\": \"publishing\",\n" +
                                        "    \"detail\": \"baz\"\n" +
                                        "  },\n" +
                                        "\n" +
                                        "  {\n" +
                                        "    \"eid\": \"eid2\",\n" +
                                        "    \"publishing_status\": \"failed\",\n" +
                                        "    \"step\": \"publishing\",\n" +
                                        "    \"detail\": \"baz\"\n" +
                                        "  },\n" +
                                        "  {\n" +
                                        "    \"eid\": \"eid3\",\n" +
                                        "    \"publishing_status\": \"submitted\",\n" +
                                        "    \"step\": \"validating\",\n" +
                                        "    \"detail\": \"baz\"\n" +
                                        "  }\n" +
                                        "]")

                );

        client = NakadiClient.builder(URI.create("http://localhost:1080/"),
                        new ProblemHandlingRequestFactory(new SimpleRequestFactory(ContentEncoding.GZIP)))
                .withCursorManager(mock(CursorManager.class))
                .withPublishingRetryStrategyAndBackoff(PublishingRetryStrategy.FULL, new ExponentialBackoffStrategy(
                        DEFAULT_INITIAL_DELAY,
                        DEFAULT_BACKOFF_FACTOR,
                        DEFAULT_MAX_DELAY,
                        3
                ))
                .build();

        assertThrows(EventPersistenceException.class,
                () -> client.publish("foobar", List.of(
                        new SomeEvent("eid1", new Metadata("eid1", OffsetDateTime.now())),
                        new SomeEvent("eid2", new Metadata("eid2", OffsetDateTime.now())),
                        new SomeEvent("eid3", new Metadata("eid3", OffsetDateTime.now())))
                ));
        clientAndServer.verify(request().withPath("/event-types/foobar/events"), exactly(4));
    }

    @Test
    void shouldStopRetryingAfterRetryExhausted() {

        clientAndServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/event-types/foobar/events")
                                .withBody(jsonPath("$[*].metadata[?(@.size() == 1)]"))
                                .withBody(jsonPath("$[0].metadata[?(@.eid == 'eid1')]"))

                ).respond(
                        response()
                                .withStatusCode(207)
                                .withContentType(APPLICATION_JSON)
                                .withBody("[\n" +
                                        "  {\n" +
                                        "    \"eid\": \"eid1\",\n" +
                                        "    \"publishing_status\": \"aborted\",\n" +
                                        "    \"step\": \"publishing\",\n" +
                                        "    \"detail\": \"baz\"\n" +
                                        "  }\n" +
                                        "\n" +
                                        "]")
                );


        assertThrows(EventPersistenceException.class, () -> {
            client.publish("foobar", List.of(
                            new SomeEvent("eid1", new Metadata("eid1", OffsetDateTime.now())))
            );
        });
        clientAndServer.verify(request().withPath("/event-types/foobar/events"), exactly(4));

    }

    @Test
    void shouldNotRetryForAuthenticationError() {

        clientAndServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/event-types/foobar/events")
                                .withBody(jsonPath("$[*].metadata[?(@.size() == 1)]"))
                                .withBody(jsonPath("$[0].metadata[?(@.eid == 'eid1')]"))

                ).respond(
                        response()
                                .withStatusCode(401)
                );


        Throwable expectedException = assertThrows(HttpRetryException.class, () -> {
            client.publish("foobar", List.of(
                            new SomeEvent("eid1", new Metadata("eid1", OffsetDateTime.now())))
            );
        });

        assertEquals("cannot retry due to server authentication, in streaming mode", expectedException.getMessage());
        clientAndServer.verify(request().withPath("/event-types/foobar/events"), exactly(1));

    }

    @Test
    void shouldNotRetryForBadRequest() {
        clientAndServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/event-types/foobar/events")
                                .withBody(jsonPath("$[*].metadata[?(@.size() == 1)]"))
                                .withBody(jsonPath("$[0].metadata[?(@.eid == 'eid1')]"))

                ).respond(
                        response()
                                .withStatusCode(400)
                );


        Throwable expectedException = assertThrows(IOException.class, () -> {
            client.publish("foobar", List.of(
                            new SomeEvent("eid1", new Metadata("eid1", OffsetDateTime.now())))
            );
        });

        assertEquals("Server returned HTTP response code: 400 for URL: " +
                "http://localhost:1080/event-types/foobar/events", expectedException.getMessage());
        clientAndServer.verify(request().withPath("/event-types/foobar/events"), exactly(1));
    }

    @Test
    void shouldNotRetryForSuccessfulResponse() throws IOException, BackoffException {

        clientAndServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/event-types/foobar/events")
                                .withBody(jsonPath("$[*].metadata[?(@.size() == 1)]"))
                                .withBody(jsonPath("$[0].metadata[?(@.eid == 'eid1')]"))

                ).respond(
                        response()
                                .withStatusCode(200)
                );


        client.publish("foobar", List.of(
                        new SomeEvent("eid1", new Metadata("eid1", OffsetDateTime.now())))
        );
        clientAndServer.verify(request().withPath("/event-types/foobar/events"), exactly(1));
    }

    @Test
    void shouldNotRetryForNotFoundException() {

        clientAndServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/event-types/foobar/events")
                                .withBody(jsonPath("$[*].metadata[?(@.size() == 1)]"))
                                .withBody(jsonPath("$[0].metadata[?(@.eid == 'eid1')]"))

                ).respond(
                        response()
                                .withStatusCode(404)
                );


        Throwable expectedException = assertThrows(FileNotFoundException.class, () -> {
            client.publish("foobar", List.of(
                            new SomeEvent("eid1", new Metadata("eid1", OffsetDateTime.now())))
            );
        });

        assertEquals("http://localhost:1080/event-types/foobar/events", expectedException.getMessage());
        clientAndServer.verify(request().withPath("/event-types/foobar/events"), exactly(1));

    }

    @Test
    void shouldNotRetryForEventValidationException() {

        clientAndServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/event-types/foobar/events")
                                .withBody(jsonPath("$[*].metadata[?(@.size() == 1)]"))
                                .withBody(jsonPath("$[0].metadata[?(@.eid == 'eid1')]"))

                ).respond(
                        response()
                                .withStatusCode(422)
                );


        Throwable expectedException = assertThrows(IOException.class, () -> {
            client.publish("foobar", List.of(
                            new SomeEvent("eid1", new Metadata("eid1", OffsetDateTime.now())))
            );
        });

        assertEquals(
                "Server returned HTTP response code: 422 for URL: http://localhost:1080/event-types/foobar/events",
                expectedException.getMessage()
        );
        clientAndServer.verify(request().withPath("/event-types/foobar/events"), exactly(1));

    }

    public static class SomeEvent implements Event {
        private final String id;
        private Metadata metadata;

        public SomeEvent(String id, final Metadata metadata) {
            this.id = id;
            this.metadata = metadata;
        }

        @Override
        public Metadata getMetadata() {
            return metadata;
        }
    }
}