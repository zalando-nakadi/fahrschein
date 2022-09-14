package org.zalando.fahrschein.opentelemetry;

import java.util.function.Consumer;
import java.util.function.Function;

import org.zalando.fahrschein.domain.Event;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class OpenTelemetryWrapper {
	
	private Tracer tracer;
	
	private String operationName;
	
	public OpenTelemetryWrapper(Tracer tracer, String operationName) {
		this.tracer = tracer;
		this.operationName = operationName;
	}

	public <R, T extends Event> R process(T event, Function<T, R> f) {
		Context context = OpenTelemetryHelper.extractFromMetadata(event.getMetadata());
		Span span = tracer.spanBuilder(operationName).setParent(context).setSpanKind(SpanKind.CONSUMER).startSpan();
		try (Scope scope = span.makeCurrent()) {
			return f.apply(event);
		} finally {
			span.end();
		}
	}

	public <T extends Event> void process(T event, Consumer<T> c) {
		Context context = OpenTelemetryHelper.extractFromMetadata(event.getMetadata());
		Span span = tracer.spanBuilder(operationName).setParent(context).setSpanKind(SpanKind.CONSUMER).startSpan();
		try (Scope scope = span.makeCurrent()) {
			c.accept(event);
		} finally {
			span.end();
		}
	}
}
