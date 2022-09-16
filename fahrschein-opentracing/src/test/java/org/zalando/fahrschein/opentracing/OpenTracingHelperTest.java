package org.zalando.fahrschein.opentracing;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.zalando.fahrschein.domain.Metadata;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;

public class OpenTracingHelperTest {
	
	private final Tracer tracer = new MockTracer();

	@Test
	public void testConvertSpanContext() {
		Span span = tracer.buildSpan("sample-publishing-operation").start();
		span.setBaggageItem("sample-item", "John Doe");
		try (Scope scope = tracer.activateSpan(span)) {
			SpanContext ctx = tracer.activeSpan().context();
			
			Map<String, String> nakadiCtx = OpenTracingHelper.spanContextToMap(tracer, ctx);
			Metadata md = new Metadata(UUID.randomUUID().toString(), OffsetDateTime.now(ZoneOffset.UTC), null, nakadiCtx);
			Assertions.assertEquals(ctx.toSpanId(), md.getSpanCtx().get("spanid"), "span-id");
			Assertions.assertEquals(ctx.toTraceId(), md.getSpanCtx().get("traceid"), "trace-id");
			Assertions.assertEquals("John Doe", md.getSpanCtx().get("baggage-sample-item"));
		} finally {
			span.finish();
		}
	}
	
	@Test
	public void testExtractSpanContext() {
		Map<String, String> nakadiCtx = new HashMap<>();
		nakadiCtx.put("traceid", "1");
		nakadiCtx.put("spanid", "2");
		nakadiCtx.put("baggage-sample-item", "John Doe");
		
		Metadata md = new Metadata(UUID.randomUUID().toString(), OffsetDateTime.now(ZoneOffset.UTC), null, nakadiCtx);
		SpanContext spCtx = OpenTracingHelper.mapToSpanContext(tracer, md.getSpanCtx());
		
		Span span = tracer.buildSpan("sample-consuming-operation").asChildOf(spCtx).start();
		try (Scope scope = tracer.activateSpan(span)) {
			SpanContext ctx = span.context();
			Assertions.assertEquals(nakadiCtx.get("traceid"), ctx.toTraceId(), "trace-id");
			Assertions.assertNotEquals(nakadiCtx.get("spanid"), ctx.toSpanId(), "span-id");
			Assertions.assertEquals("John Doe", span.getBaggageItem("sample-item"));
		} finally {
			span.finish();
		}
	}
	
}
