package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SalesOrderPlaced {
    private final SalesOrder salesOrder;

    @JsonCreator
    public SalesOrderPlaced(@JsonProperty("sales_order") SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
    }

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }
}
