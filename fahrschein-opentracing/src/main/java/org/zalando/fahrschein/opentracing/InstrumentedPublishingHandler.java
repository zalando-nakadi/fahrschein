package org.zalando.fahrschein.opentracing;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.EventPublishingHandler;

import java.util.List;
import java.util.Locale;

import static io.opentracing.tag.Tags.ERROR;

public class InstrumentedPublishingHandler implements EventPublishingHandler {

    private static final Logger LOG = LoggerFactory.getLogger(InstrumentedPublishingHandler.class);

    private final Tracer tracer;

    public InstrumentedPublishingHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public <T> void onPublish(String eventName, List<T> events) {
        try {
            Span start = tracer.buildSpan("send_" + eventName)
                    .asChildOf(tracer.activeSpan())
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_PRODUCER)
                    .withTag("messaging.destination_kind", "topic")
                    // note that OpenTracing actually defines "message_bus.destination" but we stick to the
                    // semantic naming conventions of OpenTelemetry for consistency across services.
                    .withTag("messaging.destination", eventName)
                    // message payload size in number of entities, of a given messaging operation (producing or consuming).
                    // The value should be in buckets (e.g.: 1-10). Based on internal guidance for tracing of messaging systems
                    .withTag("messaging.message_payload_size", sizeBucket(events.size()))
                    .withTag("messaging.system", "Nakadi")
                    .start();

            tracer.activateSpan(start);
        } catch (Exception e) {
            LOG.error("Exception during onPublish handling", e);
        }
    }

    @Override
    public void afterPublish() {
        try {
            tracer.activeSpan().finish();
        } catch (Exception e) {
            LOG.error("Exception during afterPublish handling", e);
        }
    }

    @Override
    public <T> void onError(List<T> events, Throwable t) {
        try {
            tracer.activeSpan().setTag(ERROR, true);
            tracer.activeSpan().setTag("message", t.getMessage());
        } catch (Exception e) {
            LOG.error("Exception during onError handling", e);
        }
    }

    // changes must be applied to both OpenTracing and OpenTelemetry implementations.
    String sizeBucket(int size) {
        if (size == 0) return "0";
        return String.format(Locale.ENGLISH, "%.0f-%.0f",
                Math.floor((size - 1) / 10f) * 10 + 1,
                Math.ceil((size) / 10f) * 10);
    }

}
