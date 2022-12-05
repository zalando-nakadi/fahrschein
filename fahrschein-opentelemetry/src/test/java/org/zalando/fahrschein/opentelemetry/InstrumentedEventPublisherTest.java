package org.zalando.fahrschein.opentelemetry;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.data.StatusData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.fahrschein.EventPublisher;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InstrumentedEventPublisherTest {

    @RegisterExtension
    static final CustomOpenTelemetryExtension otelTesting = CustomOpenTelemetryExtension.create();
    private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer(OpenTelemetryHelperTest.class.getName());

    @Mock
    private EventPublisher eventPublisher;

    @Test
    public void successfulPublish() throws IOException {
        // given
        InstrumentedEventPublisher p = new InstrumentedEventPublisher(eventPublisher, tracer);

        // when
        p.publish("test_event", Arrays.asList("ev1", "ev2"));

        // then
        verify(eventPublisher).publish(eq("test_event"), eq(Arrays.asList("ev1", "ev2")));
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
    public void failedPublish() throws IOException {
        // given
        InstrumentedEventPublisher p = new InstrumentedEventPublisher(eventPublisher, tracer);
        doThrow(new IOException("something went wrong")).when(eventPublisher).publish(anyString(), anyList());

        // when
        assertThrows(IOException.class, () ->
                p.publish("test_event", Arrays.asList("ev1", "ev2")));

        // then
        verify(eventPublisher).publish(eq("test_event"), eq(Arrays.asList("ev1", "ev2")));
        otelTesting.assertTraces()
                .hasTracesSatisfyingExactly(
                        traceAssert ->
                                traceAssert.hasSpansSatisfyingExactly(
                                        spanDataAssert ->
                                                spanDataAssert
                                                        .hasName("send_test_event")
                                                        .hasStatus(StatusData.error())));
    }

    @Test
    public void testBucketing() {
        assertEquals("0", InstrumentedEventPublisher.sizeBucket(0));
        assertEquals("1-10", InstrumentedEventPublisher.sizeBucket(1));
        assertEquals("1-10", InstrumentedEventPublisher.sizeBucket(2));
        assertEquals("1-10", InstrumentedEventPublisher.sizeBucket(10));
        assertEquals("11-20", InstrumentedEventPublisher.sizeBucket(11));
        assertEquals("101-110", InstrumentedEventPublisher.sizeBucket(104));
        assertEquals("2101-2110", InstrumentedEventPublisher.sizeBucket(2101));
        assertEquals("2101-2110", InstrumentedEventPublisher.sizeBucket(2105));
    }
}
