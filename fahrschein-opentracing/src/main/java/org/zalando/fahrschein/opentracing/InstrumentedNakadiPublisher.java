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
                .withTag("messaging.message_payload_size", sizeBucket(events.size()))
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

    // @VisibleForTesting
    // changes must be applied to both OpenTracing and OpenTelemetry implementations.
    static String sizeBucket(int size) {
       if (size == 0) return "0";
        return String.format("%.0f-%.0f",
                Math.floor((size - 1) / 10f) * 10 + 1,
                Math.ceil((size) / 10f) * 10);
    }

}
