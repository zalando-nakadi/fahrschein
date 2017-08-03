package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SalesOrder {
    private final String orderNumber;
    private final String createdAt;
    private final Money grandTotal;

    @JsonCreator
    public SalesOrder(@JsonProperty("order_number") String orderNumber, @JsonProperty("created_at") String createdAt, @JsonProperty("grand_total") Money grandTotal) {
        this.orderNumber = orderNumber;
        this.createdAt = createdAt;
        this.grandTotal = grandTotal;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Money getGrandTotal() {
        return grandTotal;
    }
}
