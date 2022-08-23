package org.zalando.fahrschein.opentelemetry;

import java.util.HashMap;
import java.util.Map;

import org.zalando.fahrschein.domain.Metadata;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;

/**
 * OpenTracing support class for Fahrschein Nakadi client. This class provides
 * helper methods for setting up OpenTracing.
 */
public class OpenTelemetryWrapper {

	public OpenTelemetryWrapper() {
	}

	/**
	 * Extracts the trace and baggage context from the given carrier map.
	 * 
	 * @param tracer        the tracer
	 * @param nakadiContext the nakadi context
	 * @return
	 */
	public static Context extractFromMetadata(Metadata metadata) {
		Map<String, String> carrier = metadata.getSpanCtx();
		if (carrier == null || carrier.isEmpty()) {
			return Context.current();
		}

		OpenTelemetry ot = GlobalOpenTelemetry.get();

		TextMapPropagator propagator = ot.getPropagators().getTextMapPropagator();
		return propagator.extract(Context.current(), carrier, new TextMapGetter<Map<String, String>>() {
			public String get(Map<String, String> carrier, String key) {
				return carrier.get(key);
			};
			
			@Override
			public Iterable<String> keys(Map<String, String> carrier) {
				return carrier.keySet();
			}
		});
	}
	
	/**
	 * Converts the given span context into a nakadi context that can be added to
	 * the metadata of the nakadi event.
	 * 
	 * @param tracer      the tracer the tracer instance
	 * @param spanContext the span context
	 * @return the nakadi context that can be added to the metadata of the nakadi
	 *         event
	 */
	public static Map<String, String> convertSpanContext(Tracer tracer, SpanContext spanContext) {
		Map<String, String> carrier = new HashMap<>();

		OpenTelemetry ot = GlobalOpenTelemetry.get();
		TextMapPropagator propagator = ot.getPropagators().getTextMapPropagator();
		
		propagator.inject(Context.current(), carrier, new TextMapSetter<Map<String, String>>() {
			@Override
			public void set(Map<String, String> carrier, String key, String value) {
				carrier.put(key, value);
			}
		});
		return carrier;
	}

}
