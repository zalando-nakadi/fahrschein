package org.zalando.fahrschein.opentelemetry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.zalando.fahrschein.NakadiPublisher;

import java.io.IOException;
import java.util.List;

import static io.opentelemetry.api.trace.SpanKind.PRODUCER;

public final class InstrumentedNakadiPublisher {

    private final NakadiPublisher client;
    private final Tracer tracer;

    public InstrumentedNakadiPublisher(NakadiPublisher client, Tracer tracer) {
        this.client = client;
        this.tracer = tracer;
    }

    public <T> void publish(String eventName, List<T> events, Span parentSpan) throws IOException {
        Span childSpan = tracer
                .spanBuilder("send_" + eventName)
                .setParent(Context.current().with(parentSpan))
                .setSpanKind(PRODUCER)
                .setAttribute("messaging.destination_kind", "topic")
                .setAttribute("messaging.destination", eventName)
                .setAttribute("messaging.system", "Nakadi")
                .startSpan();
        try {
            client.publish(eventName, events);
        } catch (Throwable t) {
            childSpan.setStatus(StatusCode.ERROR);
            childSpan.recordException(t);
            throw t;
        } finally {
            childSpan.end();
        }
    }
}
