package org.zalando.fahrschein.opentracing;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.zalando.fahrschein.domain.Event;
import org.zalando.fahrschein.domain.Metadata;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;

public class OpenTracingWrapper {
	
	private final Tracer tracer;
	
	private final String operation;
	
	public OpenTracingWrapper(Tracer tracer, String operation) {
		this.tracer = tracer;
		this.operation = operation;
	}

	/**
	 * A wrapper that handles OpenTracing around the given function. You need to
	 * pass in the function that either does the event handling or calls the event
	 * handling service. The wrapper instance sets up the Span around this
	 * invocation.
	 * 
	 * @param <R>   the return type
	 * @param <T>   the event type
	 * @param event the event
	 * @param f     the function (e.g. lambda)
	 * @return the result from the function
	 */
	public <R, T extends Event> R process(T event, Function<T, R> f) {
		Span span = createSpan(tracer, operation, event);

		try (Scope __ = tracer.activateSpan(span)) {
			return f.apply(event);
		} finally {
			span.finish();
		}
	}

	/**
	 * A wrapper that handles OpenTracing around the given consumer. You need to
	 * pass in a consumer that does the event handling or calls the event handling
	 * service. The wrapper instance sets up the Span around this invocation.
	 * 
	 * @param <T>   the event type
	 * @param event the event
	 * @param c     the consumer
	 */
	public <T extends Event> void process(T event, Consumer<T> c) {
		Span span = createSpan(tracer, operation, event);
		try (Scope __ = tracer.activateSpan(span)) {
			c.accept(event);
		} finally {
			span.finish();
		}
	}
	
	/**
	 * Extracts a SpanContext from the given event.
	 * 
	 * @param tracer the tracer instance
	 * @param event  the event
	 * @return the SpanContext or <code>null</code> if no context was propagated
	 */
	protected static SpanContext extractSpanContext(Tracer tracer, Event event) {
		Metadata metadata = event.getMetadata();
		Map<String, String> carrierData = metadata.getSpanCtx();

		return carrierData == null || carrierData.isEmpty() ? null
				: OpenTracingHelper.mapToSpanContext(tracer, carrierData);
	}

	/**
	 * Creates a newly started span instance using the span context in the nakadi
	 * event as parent.
	 * 
	 * @param tracer    the tracer instance
	 * @param operation the operation of the span
	 * @param event     the nakadi event
	 * @return the newly-started Span instance, which has *not* been automatically
	 *         registered via the {@link ScopeManager}
	 */
	protected static Span createSpan(Tracer tracer, String operation, Event event) {
		SpanContext spanContext = extractSpanContext(tracer, event);

		Span span;
		if (spanContext != null) {
			span = tracer.buildSpan(operation).asChildOf(spanContext)
					.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER).start();
		} else {
			span = tracer.buildSpan(operation).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER).start();
		}
		return span;
	}
}
