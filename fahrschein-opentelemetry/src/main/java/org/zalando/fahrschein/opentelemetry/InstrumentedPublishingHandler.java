package org.zalando.fahrschein.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.zalando.fahrschein.http.api.EventPublishingHandler;
import org.zalando.fahrschein.http.api.Response;

import java.util.List;
import java.util.Locale;

import static io.opentelemetry.api.trace.SpanKind.PRODUCER;

/**
 * Instruments publishing requests to Nakadi by making use of OpenTelemetry.
 */
public class InstrumentedPublishingHandler implements EventPublishingHandler {

    private final Tracer tracer;

    public InstrumentedPublishingHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public <T> void onPublish(String eventName, List<T> events) {
        Span span = tracer
                .spanBuilder("send_" + eventName)
                .setParent(Context.current())
                .setSpanKind(PRODUCER)
                .setAttribute("messaging.destination_kind", "topic")
                .setAttribute("messaging.destination", eventName)
                .setAttribute("messaging.system", "Nakadi")
                .setAttribute("messaging.message_payload_size", sizeBucket(events.size()))
                .startSpan();
        span.makeCurrent();
    }

    public void afterPublish() {
        Span.current().end();
    }

    @Override
    public <T> void onError(List<T> events, Throwable t) {
        Span.current().setStatus(StatusCode.ERROR).recordException(t).end();
    }

    // changes must be applied to both OpenTracing and OpenTelemetry implementations.
    String sizeBucket(int size) {
        if (size == 0) return "0";
        return String.format(Locale.ENGLISH, "%.0f-%.0f",
                Math.floor((size - 1) / 10f) * 10 + 1,
                Math.ceil((size) / 10f) * 10);
    }

}
