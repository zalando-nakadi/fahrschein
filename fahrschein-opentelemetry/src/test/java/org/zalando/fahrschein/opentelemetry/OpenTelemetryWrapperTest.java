package org.zalando.fahrschein.opentelemetry;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.extension.trace.propagation.OtTracePropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class OpenTelemetryWrapperTest {

	private static final OpenTelemetrySdk otelTesting;

	private final Tracer tracer = otelTesting.getTracer("test");

	static {
		/*
		 * This code is taken from Junit5 OpenTelementryExtension and adapted to use the
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

				Map<String, String> carrierContext = OpenTelemetryWrapper.convertSpanContext(tracer,
						span.getSpanContext());
				Assertions.assertNotNull(carrierContext);
				Assertions.assertEquals(span.getSpanContext().getTraceId().substring(TraceId.getLength() / 2), carrierContext.get("ot-tracer-traceid"));
				Assertions.assertEquals(span.getSpanContext().getSpanId(), carrierContext.get("ot-tracer-spanid"));
				Assertions.assertEquals(span.getSpanContext().getTraceFlags().isSampled() ? "true" : "false",
						carrierContext.get("ot-tracer-sampled"));
				Assertions.assertEquals("John Doe", carrierContext.get("ot-baggage-sample-item"));
			}
		} finally {
			span.end();
		}
	}

	public void testExtractContext() {

	}
}
