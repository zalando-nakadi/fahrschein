package org.zalando.fahrschein.example.domain;

import org.zalando.fahrschein.domain.Event;
import org.zalando.fahrschein.domain.Metadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SalesOrderPlaced implements Event {
	
	private final Metadata metadata;
    private final SalesOrder salesOrder;

    @JsonCreator
    public SalesOrderPlaced(
    		@JsonProperty("sales_order") SalesOrder salesOrder,
    		@JsonProperty("metadata") Metadata metadata) {
    	this.metadata = metadata;
        this.salesOrder = salesOrder;
    }

    public Metadata getMetadata() {
    	return metadata;
    }
    
    public SalesOrder getSalesOrder() {
        return salesOrder;
    }
}
