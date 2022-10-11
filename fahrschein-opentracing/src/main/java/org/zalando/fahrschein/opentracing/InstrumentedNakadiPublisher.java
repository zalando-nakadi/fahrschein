package org.zalando.fahrschein.opentracing;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.zalando.fahrschein.NakadiPublisher;

import java.io.IOException;
import java.util.List;

public final class InstrumentedNakadiPublisher {
    private final NakadiPublisher client;
    private final Tracer tracer;

    public InstrumentedNakadiPublisher(NakadiPublisher client, Tracer tracer) {
        this.client = client;
        this.tracer = tracer;
    }

    public <T> void publish(String eventName, List<T> events, Span parentSpan) throws IOException {
        Tracer.SpanBuilder childSpanBuilder = tracer.buildSpan("send_" + eventName)
                .asChildOf(parentSpan)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_PRODUCER)
                .withTag("messaging.destination_kind", "topic")
                .withTag("messaging.destination", eventName)
                .withTag("messaging.system", "Nakadi");

        Span childSpan = childSpanBuilder.start();
        try {
            client.publish(eventName, events);
        } catch (Throwable t) {
            childSpan.setTag(Tags.ERROR, true);
            throw t;
        } finally {
            childSpan.finish();
        }
    }

}
