package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by akukuljac on 27/02/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderPaymentAcceptedEvent {

    private String orderNumber;

    private String paymentMethod;

    private MetaData metaData;

    public OrderPaymentAcceptedEvent() {
    }

    @JsonCreator
    public OrderPaymentAcceptedEvent(@JsonProperty("orderNumber") String orderNumber,
                                           @JsonProperty("paymentMethod") String paymentMethod, @JsonProperty("metadata") MetaData metaData) {
        this.orderNumber = orderNumber;
        this.paymentMethod = paymentMethod;
        this.metaData = metaData;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public MetaData getMetaData() {
        return metaData;
    }


}
