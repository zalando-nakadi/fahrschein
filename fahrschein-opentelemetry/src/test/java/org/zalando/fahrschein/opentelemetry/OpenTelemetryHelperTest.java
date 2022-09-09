package org.zalando.fahrschein.opentelemetry;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zalando.fahrschein.domain.Metadata;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.extension.trace.propagation.OtTracePropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class OpenTelemetryHelperTest {

	private static final OpenTelemetrySdk otelTesting;

	private final Tracer tracer = otelTesting.getTracer("test");

	static {
		/*
		 * This code is taken from Junit5 OpenTelemetryExtension and adapted to use the
		 * OtTracePropagator instead of the W3CPropagator
		 */
		InMemorySpanExporter spanExporter = InMemorySpanExporter.create();

		SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
				.addSpanProcessor(SimpleSpanProcessor.create(spanExporter)).build();

		InMemoryMetricReader metricReader = InMemoryMetricReader.create();

		SdkMeterProvider meterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader).build();

		otelTesting = OpenTelemetrySdk.builder()
				.setPropagators(ContextPropagators.create(OtTracePropagator.getInstance()))
				.setTracerProvider(tracerProvider).setMeterProvider(meterProvider).build();
	}

	@BeforeAll
	public static void beforeAll() {
		GlobalOpenTelemetry.resetForTest();
		GlobalOpenTelemetry.set(otelTesting);
	}

	@AfterAll
	public static void afterAll() {
		GlobalOpenTelemetry.resetForTest();
	}

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
		
		Metadata metadata = new Metadata("sample-eid", OffsetDateTime.now(), "sample-flow-id", carrierContext);
		
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
