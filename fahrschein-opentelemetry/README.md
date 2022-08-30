# OpenTelemetry Support

This module provides support for propagating OpenTracing context over Nakadi Events.

## Event Publishing

The event publishing support injects the OpenTelementry context using a TextMap propagator to the metadata of the Nakadi event.

When you create the event you can use the support class as shown in the following code snippet for setting up the event metadata.

```
SpanContext ctx = tracer.activeSpan().context();
Metadata md = new Metadata(UUID.randomUUID().toString(), OffsetDateTime.now(),
    "sample-flow-id", OpenTelemetryWrapper.convertSpanContext(tracer, ctx));
```

## Event Consumption

The event consumption supports extracts OpenTelemetry context using a TextMap propagator from the metadata of the Nakadi event.

When you process an event consumed as part of the batch you can use the support class as shown in the following code snippet for setting up the OpenTelementry Span.

```
Context parentContext = OpenTelementryWrapper.extractFromMetadata(event.getMetadata());
Span span = tracer.spanBuilder("sample-consuming-operation")
    .setParent(eventContext)
    .setSpanKind(SpanKind.CONSUMER)
    .startSpan();
try (Scope scope = span.makeCurrent()) {
  SpanContext ctx = span.context();
  // process the event
  ...
} finally {
  span.end();
}
```
