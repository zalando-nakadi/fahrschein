package org.zalando.fahrschein.opentracing;

import java.util.HashMap;
import java.util.Map;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;

/**
 * OpenTracing support class for Fahrschein Nakadi client. This class provides
 * helper methods for setting up OpenTracing.
 */
public class OpenTracingHelper {

	private OpenTracingHelper() {
	}

	/**
	 * Converts the given nakadi specific representation to a span context that can
	 * be used as parent for new spans.
	 * 
	 * @param tracer        the tracer
	 * @param nakadiContext the nakadi context
	 * @return The {@code SpanContext} for the given NakadiContext
	 */
	public static SpanContext mapToSpanContext(Tracer tracer, Map<String, String> nakadiContext) {
		return tracer.extract(Format.Builtin.TEXT_MAP_EXTRACT, new TextMapExtractAdapter(nakadiContext));
	}

	/**
	 * Converts the given span context into a Nakadi context that can be added to
	 * the metadata of the Nakadi event.
	 * 
	 * @param tracer      the tracer instance
	 * @param spanContext the span context
	 * @return the nakadi context that can be added to the metadata of the Nakadi
	 *         event
	 */
	public static Map<String, String> spanContextToMap(Tracer tracer, SpanContext spanContext) {
		Map<String, String> nakadiContext = new HashMap<>();
		tracer.inject(spanContext, Format.Builtin.TEXT_MAP_INJECT, new TextMapInjectAdapter(nakadiContext));
		return nakadiContext;
	}

}
