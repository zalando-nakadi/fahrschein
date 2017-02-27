package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by akukuljac on 27/02/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCreatedEvent {

    private String orderNumber;

    private String customerNumber;

    private Integer grandTotal;

    private MetaData metaData;

    public OrderCreatedEvent() {
    }

    @JsonCreator
    public OrderCreatedEvent(@JsonProperty("orderNumber") String orderNumber,
                             @JsonProperty("grandTotal") Integer grandTotal,
                             @JsonProperty("customerNumber") String customerNumber,
                             @JsonProperty("metadata") MetaData metaData) {
        this.orderNumber = orderNumber;
        this.grandTotal = grandTotal;
        this.customerNumber = customerNumber;
        this.metaData = metaData;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Integer getGrandTotal() {
        return grandTotal;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public MetaData getMetaData() {
        return metaData;
    }


}
