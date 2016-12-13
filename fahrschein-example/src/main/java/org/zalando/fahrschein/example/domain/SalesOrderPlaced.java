package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SalesOrderPlaced {
    private final SalesOrder salesOrder;

    @JsonCreator
    public SalesOrderPlaced(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
    }

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }
}
