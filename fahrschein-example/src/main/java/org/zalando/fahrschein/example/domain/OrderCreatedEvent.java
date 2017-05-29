package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.domain.Metadata;

@JsonTypeName("eventlog.e96001_order_created")
public class OrderCreatedEvent extends OrderEvent {

    private static final Logger LOG = LoggerFactory.getLogger(OrderCreatedEvent.class);

    private final Metadata metadata;
    private final String orderNumber;
    private final String customerNumber;
    private final Integer grandTotal;
    private final String paymentMethod;

    @JsonCreator
    public OrderCreatedEvent(@JsonProperty("metadata") Metadata metadata,
                             @JsonProperty("orderNumber") String orderNumber,
                             @JsonProperty("grandTotal") Integer grandTotal,
                             @JsonProperty("customerNumber") String customerNumber,
                             @JsonProperty("paymentMethod") String paymentMethod) {
        this.metadata = metadata;
        this.orderNumber = orderNumber;
        this.grandTotal = grandTotal;
        this.customerNumber = customerNumber;
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
        processor.processOrderCreated(this);
    }

    public Integer getGrandTotal() {
        return grandTotal;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
