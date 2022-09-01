# OpenTracing Support

This module provides support for propagating OpenTracing contexts over Nakadi Events.

## Event Publishing

The event publishing support injects the OpenTracing span context using a TextMap propagator to the metadata of the Nakadi event.

When you create the event you can use the support class as shown in the following code snippet for setting up the event metadata.

```
SpanContext ctx = tracer.activeSpan().context();
Metadata md = new Metadata(UUID.randomUUID().toString(), OffsetDateTime.now(),
    "sample-flow-id", OpenTracingWrapper.convertSpanContext(tracer, ctx));
```

## Event Consumption

The event consumption supports extracting the OpenTracing span context using a TextMap propagator from the metadata of the Nakadi event.

When you process an event consumed as part of the batch, you can use the support class as shown in the following code snippet for setting up the OpenTracing Span.

```
SpanContext spCtx = OpenTracingWrapper.convertNakadiContext(tracer, event.getMetadata().getSpanCtx());
Span span = tracer.buildSpan("sample-consuming-operation").asChildOf(spCtx).start();
try (Scope scope = tracer.activateSpan(span)) {
  SpanContext ctx = span.context();
  // process the event
  ...
} finally {
  span.finish();
}
```
