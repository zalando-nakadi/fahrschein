# OpenTelemetry Support

This module provides support for propagating OpenTelemetry contexts over Nakadi Events.

## Event Publishing

The event publishing support injects the OpenTelementry context using a TextMap propagator to the metadata of the Nakadi event.

When you create the event you can use the support class as shown in the following code snippet for setting up the event metadata.

```java
SpanContext ctx = tracer.activeSpan().context();
Metadata md = new Metadata(UUID.randomUUID().toString(), OffsetDateTime.now(),
    "sample-flow-id", OpenTelemetryWrapper.convertSpanContext(tracer, ctx));
```

## Event Consumption

The event consumption supports extracting the OpenTelemetry span context using a TextMap propagator from the metadata of the Nakadi event.

When you process an event consumed as part of the batch, you can use the support class as shown in the following code snippet for setting up the OpenTelemetry span.

```java
Context parentContext = OpenTelemetryWrapper.extractFromMetadata(event.getMetadata());
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
