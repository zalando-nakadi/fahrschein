package org.zalando.fahrschein.opentelemetry;

import java.util.HashMap;
import java.util.Map;

import org.zalando.fahrschein.domain.Metadata;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;

/**
 * OpenTelemetry support class for Fahrschein Nakadi client. This class provides
 * helper methods for setting up OpenTelemetry.
 */
public class OpenTelemetryHelper {

	private OpenTelemetryHelper() {
	}

	/**
	 * Extracts the trace and baggage context from the given carrier map.
	 *
	 * @param metadata the event metadata
	 * @return the OpenTelemetry context ready to be set as current
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
			}
			
			@Override
			public Iterable<String> keys(Map<String, String> carrier) {
				return carrier.keySet();
			}
		});
	}
	
	/**
	 * Converts the current context into a Nakadi context that can be added to
	 * the metadata of the Nakadi event.
	 *
	 * @return the Nakadi context that can be added to the metadata of the Nakadi
	 *         event
	 */
	public static Map<String, String> currentContextToMap() {
		Map<String, String> carrier = new HashMap<>();

		OpenTelemetry ot = GlobalOpenTelemetry.get();
		TextMapPropagator propagator = ot.getPropagators().getTextMapPropagator();
		
		propagator.inject(Context.current(), carrier, (carrier1, key, value) -> carrier1.put(key, value));
		return carrier;
	}

}
