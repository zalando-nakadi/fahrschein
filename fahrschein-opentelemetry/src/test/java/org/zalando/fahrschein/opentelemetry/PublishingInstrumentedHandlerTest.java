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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.Response;


import java.net.URI;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishingInstrumentedHandlerTest {

    @Mock
    private Request request;

    @Mock
    private Response response;

    @RegisterExtension
    static final CustomOpenTelemetryExtension otelTesting = CustomOpenTelemetryExtension.create();
    private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer(OpenTelemetryHelperTest.class.getName());

    private PublishingInstrumentedHandler instrumentedHandler;

    @BeforeEach
    public void before() {
        instrumentedHandler = new PublishingInstrumentedHandler(tracer);
    }

    @AfterEach
    public void after() {
        otelTesting.clearSpans();
    }
    @Test
    public void beforePublishTest() {
        // given
        when(request.getURI()).thenReturn(URI.create("https://nakadi.io/event-types/test_event/events"));

        // when
        instrumentedHandler.beforeExecute(request);
        //Finish trace
        instrumentedHandler.afterExecute(request, response);

        otelTesting.getSpans();
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
                                                                        AttributeKey.stringKey("messaging.system"), "Nakadi"))));
    }

        @Test
        public void onErrorTest() {
            // given
            when(request.getURI()).thenReturn(URI.create("https://nakadi.io/event-types/test_event/events"));

            // when
            instrumentedHandler.beforeExecute(request);
            instrumentedHandler.onError(request, new Throwable("Something is wrong"));

            // then
            otelTesting.assertTraces()
                    .hasTracesSatisfyingExactly(
                            traceAssert ->
                                    traceAssert.hasSpansSatisfyingExactly(
                                            spanDataAssert ->
                                                    spanDataAssert
                                                            .hasName("send_test_event")
                                                            .hasStatus(StatusData.error())));
        }

}