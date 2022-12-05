package org.zalando.fahrschein.opentracing;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.fahrschein.EventPublisher;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InstrumentedEventPublisherTest {

    private final MockTracer tracer = new MockTracer();

    @Mock
    private EventPublisher eventPublisher;

    @Test
    public void successfulPublish() throws IOException {
        // given
        InstrumentedEventPublisher p = new InstrumentedEventPublisher(eventPublisher, tracer);
        MockSpan parentSpan = tracer.buildSpan("parent").start();

        // when
        p.publish("test_event", Arrays.asList("ev1", "ev2"), parentSpan);

        // then
        verify(eventPublisher).publish(eq("test_event"), eq(Arrays.asList("ev1", "ev2")));
        assertEquals(1, tracer.finishedSpans().size(), "finished spans");
        assertEquals("send_test_event", tracer.finishedSpans().get(0).operationName(), "send_test_event");
        assertEquals("{messaging.system=Nakadi, messaging.destination=test_event, messaging.message_payload_size=1-10, messaging.destination_kind=topic, span.kind=producer}", tracer.finishedSpans().get(0).tags().toString(), "send_test_event tags");
    }

    @Test
    public void failedPublish() throws IOException {
        // given
        InstrumentedEventPublisher p = new InstrumentedEventPublisher(eventPublisher, tracer);
        MockSpan parentSpan = tracer.buildSpan("parent").start();
        doThrow(new IOException("something went wrong")).when(eventPublisher).publish(anyString(), anyList());

        // when
        Assertions.assertThrows(IOException.class, () ->
                p.publish("test_event", Arrays.asList("ev1", "ev2"), parentSpan));

        // then
        verify(eventPublisher).publish(eq("test_event"), eq(Arrays.asList("ev1", "ev2")));
        assertEquals(1, tracer.finishedSpans().size(), "finished spans");
        assertEquals("send_test_event", tracer.finishedSpans().get(0).operationName(), "send_test_event");
        assertEquals(true, tracer.finishedSpans().get(0).tags().get(Tags.ERROR.getKey()), "send_test_event error=true");
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
