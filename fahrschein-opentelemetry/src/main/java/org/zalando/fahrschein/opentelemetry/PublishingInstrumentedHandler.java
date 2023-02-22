package org.zalando.fahrschein.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestHandler;
import org.zalando.fahrschein.http.api.Response;

import static io.opentelemetry.api.trace.SpanKind.PRODUCER;

/**
 * Instruments publishing requests to Nakadi.
 */
public class PublishingInstrumentedHandler implements RequestHandler {

    private static final String EVENT_TYPES = "/event-types/";
    private final Tracer tracer;

    public PublishingInstrumentedHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void beforeExecute(Request request) {
        String eventName = extractEventName(request.getURI().getPath());

        tracer
                .spanBuilder("send_" + eventName)
                .setParent(Context.current())
                .setSpanKind(PRODUCER)
                .setAttribute("messaging.destination_kind", "topic")
                .setAttribute("messaging.destination", eventName)
                .setAttribute("messaging.system", "Nakadi")
                .startSpan();
    }

    @Override
    public void afterExecute(Request request, Response response) {
        Span.current().end();
    }

    @Override
    public void onError(Request request, Throwable t) {
        Span.current().setStatus(StatusCode.ERROR).end();
    }

    String extractEventName(String path) {
       return path.substring(path.lastIndexOf(EVENT_TYPES)+EVENT_TYPES.length(), path.indexOf("/events"));
    }
}
