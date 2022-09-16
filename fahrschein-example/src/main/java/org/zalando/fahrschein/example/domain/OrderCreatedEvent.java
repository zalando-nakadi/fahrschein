package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.zalando.fahrschein.EventProcessingException;
import org.zalando.fahrschein.domain.Metadata;

import javax.money.MonetaryAmount;

@JsonTypeName("order_created")
public class OrderCreatedEvent extends OrderEvent {

    private final Metadata metadata;
    private final String orderNumber;
    private final String customerNumber;
    private final MonetaryAmount grandTotal;
    private final String paymentMethod;

    @JsonCreator
    public OrderCreatedEvent(@JsonProperty("metadata") Metadata metadata,
                             @JsonProperty("orderNumber") String orderNumber,
                             @JsonProperty("grandTotal") MonetaryAmount grandTotal,
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
    public void process(final MultiEventProcessor processor) throws EventProcessingException {
        processor.process(this);
    }

    public MonetaryAmount getGrandTotal() {
        return grandTotal;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
