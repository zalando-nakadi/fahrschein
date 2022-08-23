package org.zalando.fahrschein.opentelemetry;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
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
		try (Scope scope = span.makeCurrent()) {
			Map<String, String> carrierContext = OpenTelemetryWrapper.convertSpanContext(tracer, span.getSpanContext());
			Assertions.assertNotNull(carrierContext);
			
		} finally {
			span.end();
		}
	}

	public void testExtractContext() {

	}
}
