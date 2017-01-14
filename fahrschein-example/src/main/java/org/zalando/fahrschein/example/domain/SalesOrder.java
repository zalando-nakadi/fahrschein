package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.money.MonetaryAmount;
import java.time.OffsetDateTime;

public class SalesOrder {
    private final String orderNumber;
    private final OffsetDateTime createdAt;
    private final MonetaryAmount grandTotal;

    @JsonCreator
    public SalesOrder(String orderNumber, OffsetDateTime createdAt, MonetaryAmount grandTotal) {
        this.orderNumber = orderNumber;
        this.createdAt = createdAt;
        this.grandTotal = grandTotal;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public MonetaryAmount getGrandTotal() {
        return grandTotal;
    }
}
