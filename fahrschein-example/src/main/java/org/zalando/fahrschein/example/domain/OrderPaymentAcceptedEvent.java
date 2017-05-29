package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.domain.Metadata;

@JsonTypeName("eventlog.e62001_order_payment_status_accepted")
public class OrderPaymentAcceptedEvent extends OrderEvent {

    private static final Logger LOG = LoggerFactory.getLogger(OrderPaymentAcceptedEvent.class);

    private final Metadata metadata;
    private final String orderNumber;
    private final String paymentMethod;

    @JsonCreator
    public OrderPaymentAcceptedEvent(@JsonProperty("metadata") Metadata metadata,
                                     @JsonProperty("orderNumber") String orderNumber,
                                     @JsonProperty("paymentMethod") String paymentMethod) {
        this.metadata = metadata;
        this.orderNumber = orderNumber;
        this.paymentMethod = paymentMethod;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public String getOrderNumber() {
        return orderNumber;
    }

    @Override
    public void process(final OrderEventProcessor processor) {
        processor.processPaymentAccepted(this);
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

}
