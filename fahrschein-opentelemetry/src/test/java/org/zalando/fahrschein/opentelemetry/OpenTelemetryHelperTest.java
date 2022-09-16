package org.zalando.fahrschein.opentelemetry;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.zalando.fahrschein.domain.Metadata;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.IdGenerator;

public class OpenTelemetryHelperTest {

	@RegisterExtension
	static final CustomOpenTelemetryExtension otelTesting = CustomOpenTelemetryExtension.create();
	private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer(OpenTelemetryHelperTest.class.getName());

	@Test
	public void testInjectContext() {
		Span span = tracer.spanBuilder("name").startSpan();
		try (Scope spanScope = span.makeCurrent()) {

			// setup some baggage items
			Baggage baggage = Baggage.builder().put("sample-item", "John Doe").build();
			try (Scope baggageScope = baggage.makeCurrent()) {

				Map<String, String> carrierContext = OpenTelemetryHelper.currentContextToMap();
				Assertions.assertNotNull(carrierContext);
				// convention found in OtTracePropagator
				Assertions.assertEquals(span.getSpanContext().getTraceId().substring(TraceId.getLength() / 2), carrierContext.get("ot-tracer-traceid"));
				Assertions.assertEquals(span.getSpanContext().getSpanId(), carrierContext.get("ot-tracer-spanid"));
				Assertions.assertEquals(String.valueOf(span.getSpanContext().getTraceFlags().isSampled()),
						carrierContext.get("ot-tracer-sampled"));
				Assertions.assertEquals("John Doe", carrierContext.get("ot-baggage-sample-item"));
			}
		} finally {
			span.end();
		}
	}

	@Test
	public void testExtractContext() {
	IdGenerator idGenerator = IdGenerator.random();
	String traceId = idGenerator.generateTraceId().substring(TraceId.getLength() / 2);
	String spanId = idGenerator.generateSpanId();

	Map<String, String> carrierContext = new HashMap<>();
	// convention found in OtTracePropagator
	carrierContext.put("ot-tracer-traceid", traceId);
	carrierContext.put("ot-tracer-spanid", spanId);
	carrierContext.put("ot-tracer-sampled", "true");
	carrierContext.put("ot-baggage-sample-item", "John Doe");

	Metadata metadata = new Metadata("sample-eid", OffsetDateTime.now(ZoneOffset.UTC), "sample-flow-id", carrierContext);

		Context context = OpenTelemetryHelper.extractFromMetadata(metadata);
		context.makeCurrent();
		Span span = Span.fromContext(context);
		// convention found in the OtTracePropagator
		Assertions.assertEquals(StringUtils.padLeft(traceId, TraceId.getLength()), span.getSpanContext().getTraceId());
		Assertions.assertEquals(spanId, span.getSpanContext().getSpanId());
		Assertions.assertTrue(span.getSpanContext().getTraceFlags().isSampled());
		
		Baggage baggage = Baggage.fromContext(context);
		Assertions.assertEquals("John Doe", baggage.getEntryValue("sample-item"));
		
	}
}
