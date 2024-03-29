package org.zalando.fahrschein.opentelemetry;

import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.IdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.zalando.fahrschein.domain.Event;
import org.zalando.fahrschein.domain.Metadata;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class OpenTelemetryWrapperTest {

    @RegisterExtension
    static final CustomOpenTelemetryExtension otelTesting = CustomOpenTelemetryExtension.create();

    private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer(OpenTelemetryWrapperTest.class.getName());

    IdGenerator idGenerator = IdGenerator.random();
    final String parentTraceId = idGenerator.generateTraceId().substring(TraceId.getLength() / 2);
    final String parentSpanId = idGenerator.generateSpanId();

    @Test
    public void testInjectContextIntoConsumer() {
        OpenTelemetryWrapper wrapper = new OpenTelemetryWrapper(tracer, "process_events");
        wrapper.process(getEvent(), (event) -> {
            // process event
        });
        assertTraces();
    }

    @Test
    public void testInjectContextIntoFunction() {
        OpenTelemetryWrapper wrapper = new OpenTelemetryWrapper(tracer, "process_events");
        wrapper.process(getEvent(), (event) -> {
            // process event
            return new Object();
        });
        assertTraces();
    }

    private void assertTraces() {
        otelTesting.assertTraces()
                .hasTracesSatisfyingExactly(
                        traceAssert ->
                                traceAssert.hasSpansSatisfyingExactly(
                                        spanDataAssert ->
                                                spanDataAssert
                                                        .hasName("process_events")
                                                        .hasParentSpanId(parentSpanId)));

    }


    private Event getEvent() {
        return () -> {
            Map<String, String> carrierContext = new HashMap<>();
            // convention found in OtTracePropagator
            carrierContext.put("ot-tracer-traceid", parentTraceId);
            carrierContext.put("ot-tracer-spanid", parentSpanId);
            carrierContext.put("ot-tracer-sampled", "true");
            carrierContext.put("ot-baggage-sample-item", "John Doe");
            return new Metadata("sample-eid", OffsetDateTime.now(ZoneOffset.UTC), "sample-flow-id", carrierContext);
        };
    }

}
