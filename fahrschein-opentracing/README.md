# OpenTracing Support

This module provides support for propagating OpenTracing context over Nakadi Events.

# Event Publishing

The support for event publishing adds the OpenTracing Span Context as using Text Map propagators to the metadata of the event.

When you create the event you can use the support class as shown in the following code snippet

```

Metadata md = new Metadata("sample-event", UUID.randomUUID.toString(), )

```

# Event Consumption