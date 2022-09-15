package org.zalando.fahrschein.example;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.extension.trace.propagation.OtTracePropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class OpenTelemetrySetup {

    public static Tracer init() {
                /*
         * This code is taken from Junit5 OpenTelemetryExtension and adapted to use the
         * OtTracePropagator instead of the W3CPropagator
         */
        InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter)).build();
        OpenTelemetrySdk otelTesting = OpenTelemetrySdk.builder()
                .setPropagators(ContextPropagators.create(OtTracePropagator.getInstance()))
                .setTracerProvider(tracerProvider).build();
        GlobalOpenTelemetry.set(otelTesting);
        return otelTesting.getTracer(ConsumerExample.class.getName());
    }
}
