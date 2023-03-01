package org.zalando.fahrschein.opentelemetry;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.data.StatusData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InstrumentedPublishingHandlerTest {

    @RegisterExtension
    static final CustomOpenTelemetryExtension otelTesting = CustomOpenTelemetryExtension.create();
    private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer(OpenTelemetryHelperTest.class.getName());

    private InstrumentedPublishingHandler instrumentedHandler;

    @BeforeEach
    public void before() {
        instrumentedHandler = new InstrumentedPublishingHandler(tracer);
    }

    @AfterEach
    public void after() {
        otelTesting.clearSpans();
    }
    @Test
    public void beforePublishTest() {
        // when
        instrumentedHandler.onPublish("test_event", Arrays.asList("ev1", "ev2"));
        instrumentedHandler.afterPublish();
        // then
        otelTesting.assertTraces()
                .hasTracesSatisfyingExactly(
                        traceAssert ->
                                traceAssert.hasSpansSatisfyingExactly(
                                        spanDataAssert ->
                                                spanDataAssert
                                                        .hasName("send_test_event")
                                                        .hasAttributes(
                                                                Attributes.of(
                                                                        AttributeKey.stringKey("messaging.destination_kind"), "topic",
                                                                        AttributeKey.stringKey("messaging.destination"), "test_event",
                                                                        AttributeKey.stringKey("messaging.message_payload_size"), "1-10",
                                                                        AttributeKey.stringKey("messaging.system"), "Nakadi"))));
    }

        @Test
        public void onErrorTest() {

            Throwable somethingIsWrong = new Throwable("Something is wrong");
            // when
            instrumentedHandler.onPublish("test_event", Arrays.asList("ev1", "ev2"));
            instrumentedHandler.onError(Arrays.asList("ev1", "ev2"), somethingIsWrong);

            // then
            otelTesting.assertTraces()
                    .hasTracesSatisfyingExactly(
                            traceAssert ->
                                    traceAssert.hasSpansSatisfyingExactly(
                                            spanDataAssert ->
                                                    spanDataAssert
                                                            .hasName("send_test_event")
                                                            .hasStatus(StatusData.error())
                                                            .hasException(somethingIsWrong)));
        }

    @Test
    public void testBucketing() {
        assertEquals("0", instrumentedHandler.sizeBucket(0));
        assertEquals("1-10", instrumentedHandler.sizeBucket(1));
        assertEquals("1-10", instrumentedHandler.sizeBucket(2));
        assertEquals("1-10", instrumentedHandler.sizeBucket(10));
        assertEquals("11-20", instrumentedHandler.sizeBucket(11));
        assertEquals("101-110", instrumentedHandler.sizeBucket(104));
        assertEquals("2101-2110", instrumentedHandler.sizeBucket(2101));
        assertEquals("2101-2110", instrumentedHandler.sizeBucket(2105));
    }
}