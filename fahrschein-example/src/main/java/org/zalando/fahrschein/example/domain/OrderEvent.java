package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;
import org.zalando.fahrschein.domain.Event;
import org.zalando.fahrschein.typeresolver.MetadataTypeResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, visible = false)
@JsonTypeResolver(MetadataTypeResolver.class)
@JsonSubTypes({@JsonSubTypes.Type(OrderCreatedEvent.class), @JsonSubTypes.Type(OrderPaymentAcceptedEvent.class)})
public abstract class OrderEvent implements Event {
    public abstract String getOrderNumber();
    public abstract void process(OrderEventProcessor processor);
}
