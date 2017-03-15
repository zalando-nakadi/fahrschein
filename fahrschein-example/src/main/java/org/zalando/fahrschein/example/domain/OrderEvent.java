package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;
import org.zalando.fahrschein.MetadataTypeResolver;
import org.zalando.fahrschein.domain.Event;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, visible = false)
@JsonTypeResolver(MetadataTypeResolver.class)
@JsonSubTypes({@JsonSubTypes.Type(OrderCreatedEvent.class), @JsonSubTypes.Type(OrderPaymentAcceptedEvent.class)})
public abstract class OrderEvent implements Event {
    public abstract String getOrderNumber();
}
