package org.zalando.fahrschein.opentracing;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.zalando.fahrschein.NakadiPublisher;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static io.opentracing.tag.Tags.ERROR;

public final class InstrumentedNakadiPublisher {
    private final NakadiPublisher client;
    private final Tracer tracer;

    public InstrumentedNakadiPublisher(NakadiPublisher client, Tracer tracer) {
        this.client = client;
        this.tracer = tracer;
    }

    public <T> void publish(String eventName, List<T> events, Span parentSpan) throws IOException {
        Objects.requireNonNull(parentSpan);
        Tracer.SpanBuilder childSpanBuilder = tracer.buildSpan("send_" + eventName)
                .asChildOf(parentSpan)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_PRODUCER)
                .withTag("messaging.destination_kind", "topic")
                // note that OpenTracing actually defines "message_bus.destination" but we stick to the
                // semantic naming conventions of OpenTelemetry for consistency across services.
                .withTag("messaging.destination", eventName)
                // message payload size in number of entities, of a given messaging operation (producing or consuming).
                // The value should be in buckets (e.g.: 1-10). Based on internal guidance for tracing of messaging systems
                .withTag("messaging.message_payload_size", sizeBucket(events.size()))
                .withTag("messaging.system", "Nakadi");

        Span childSpan = childSpanBuilder.start();
        try {
            client.publish(eventName, events);
        } catch (Throwable t) {
            childSpan.setTag(ERROR, true);
            throw t;
        } finally {
            childSpan.finish();
        }
    }

    // @VisibleForTesting
    // changes must be applied to both OpenTracing and OpenTelemetry implementations.
    static String sizeBucket(int size) {
        if (size == 0) return "0";
        return String.format(Locale.ENGLISH, "%.0f-%.0f",
                Math.floor((size - 1) / 10f) * 10 + 1,
                Math.ceil((size) / 10f) * 10);
    }

}
