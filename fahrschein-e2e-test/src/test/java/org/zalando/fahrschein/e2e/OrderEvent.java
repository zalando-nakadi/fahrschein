package org.zalando.fahrschein.e2e;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.zalando.fahrschein.domain.Event;
import org.zalando.fahrschein.domain.Metadata;

public class OrderEvent implements Event {

    public final String orderNumber;
    public final Metadata metadata;

    @JsonCreator
    public OrderEvent(@JsonProperty("metadata") Metadata metadata,
                      @JsonProperty("order_number") String orderNumber) {
        this.metadata = metadata;
        this.orderNumber = orderNumber;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }
}
