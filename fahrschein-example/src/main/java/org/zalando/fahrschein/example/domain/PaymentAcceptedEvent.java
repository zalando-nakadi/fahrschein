package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.zalando.fahrschein.EventProcessingException;
import org.zalando.fahrschein.domain.Metadata;

@JsonTypeName("payment_accepted")
public class PaymentAcceptedEvent extends OrderEvent {

    private final Metadata metadata;
    private final String orderNumber;
    private final String paymentMethod;

    @JsonCreator
    public PaymentAcceptedEvent(@JsonProperty("metadata") Metadata metadata,
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
    public void process(final MultiEventProcessor processor) throws EventProcessingException {
        processor.process(this);
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

}
