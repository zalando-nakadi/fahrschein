package org.zalando.fahrschein.opentracing;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import org.junit.jupiter.api.Test;
import org.zalando.fahrschein.domain.Event;
import org.zalando.fahrschein.domain.Metadata;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpenTracingWrapperTest {

    private Long parentSpanId = 1L;
    private Long parentTraceId = 2L;

    @Test
    public void testInjectContextIntoConsumer() {
        MockTracer tracer = new MockTracer();
        OpenTracingWrapper wrapper = new OpenTracingWrapper(tracer, "process_events");
        wrapper.process(getEvent(), (event) -> {
            // process event
        });
        assertTraces(tracer);
    }

    @Test
    public void testInjectContextIntoFunction() {
        MockTracer tracer = new MockTracer();
        OpenTracingWrapper wrapper = new OpenTracingWrapper(tracer, "process_events");
        wrapper.process(getEvent(), (event) -> {
            // process event
            return new Object();
        });
        assertTraces(tracer);
    }

    private void assertTraces(MockTracer tracer) {
        List<MockSpan> spans = tracer.finishedSpans();
        ((Consumer<MockSpan>) mockSpan -> {
            assertEquals(parentSpanId, mockSpan.parentId(), "parent span Id");
            assertEquals("process_events", mockSpan.operationName());
        }).accept(spans.get(0));
        assertEquals(1, spans.size(), "number of spans");
    }

    private Event getEvent() {
        return () -> {
            Map<String, String> nakadiCtx = new HashMap<>();
            nakadiCtx.put("traceid", parentTraceId.toString());
            nakadiCtx.put("spanid", parentSpanId.toString());
            nakadiCtx.put("baggage-sample-item", "John Doe");
            return new Metadata("test-event", OffsetDateTime.now(ZoneOffset.UTC), null, nakadiCtx);
        };
    }

}
