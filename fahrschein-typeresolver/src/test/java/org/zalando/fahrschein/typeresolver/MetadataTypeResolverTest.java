package org.zalando.fahrschein.typeresolver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;
import org.junit.Assert;
import org.junit.Test;
import org.zalando.fahrschein.domain.Event;
import org.zalando.fahrschein.domain.Metadata;

import java.io.IOException;

public class MetadataTypeResolverTest {
    private static final String ORDER_SHIPPED = "{\"foo\":\"bar\",\"metadata\":{\"occurred_at\":\"2017-03-15T02:00:48.497Z\",\"eid\":\"36931a5d-74d6-3033-84ea-a8336197c4ce\",\"event_type\":\"order_shipped\",\"partition\":\"0\",\"received_at\":\"2017-03-15T02:00:52.355Z\",\"flow_id\":\"GEI8VPtEd02DSBMBnV1GjChS\",\"version\":\"0.1.0\"},\"order_number\":\"10410018540147\",\"flow_id\":\"RzhTJ2sLTVigDrxhTrlQ_Q\"}";
    private static final String ORDER_CREATED = "{\"foo\":\"baz\",\"metadata\":{\"occurred_at\":\"2017-03-15T02:00:47.689Z\",\"eid\":\"a3e25946-5ae9-3964-91fa-26ecb7588d67\",\"event_type\":\"order_created\",\"partition\":\"0\",\"received_at\":\"2017-03-15T02:00:51.437Z\",\"flow_id\":\"GH5kZY88Zkj6O5QKVnPBQbZw\",\"version\":\"0.1.0\"},\"order_number\":\"10410018540147\",\"customer_number\":\"123456\",\"shipping_country\":\"DE\",\"flow_id\":\"R5brW3FzQ16WL5vdNb0jCA\",\"payment_method\":\"INVOICE\"}";

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM)
    @JsonTypeResolver(MetadataTypeResolver.class)
    @JsonSubTypes({@JsonSubTypes.Type(OrderCreatedEvent.class), @JsonSubTypes.Type(OrderShippedEvent.class)})
    public static abstract class OrderEvent implements Event {
        abstract String getOrderNumber();

    }

    @JsonTypeName("order_created")
    public static class OrderCreatedEvent extends OrderEvent {
        private final Metadata metadata;
        private final String orderNumber;
        private final String customerNumber;
        private final String paymentMethod;

        @JsonCreator
        public OrderCreatedEvent(@JsonProperty("metadata") Metadata metadata, @JsonProperty("order_number") String orderNumber, @JsonProperty("customer_number") String customerNumber, @JsonProperty("payment_method") String paymentMethod) {
            this.metadata = metadata;
            this.orderNumber = orderNumber;
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

        public String getCustomerNumber() {
            return customerNumber;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }
    }

    @JsonTypeName("order_shipped")
    public static class OrderShippedEvent extends OrderEvent {
        private final Metadata metadata;
        private final String orderNumber;

        @JsonCreator
        public OrderShippedEvent(@JsonProperty("metadata") Metadata metadata, @JsonProperty("order_number") String orderNumber) {
            this.metadata = metadata;
            this.orderNumber = orderNumber;
        }

        @Override
        public Metadata getMetadata() {
            return metadata;
        }

        @Override
        public String getOrderNumber() {
            return orderNumber;
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void shouldDeserializeOrderShipped() throws IOException {
        final OrderEvent event = objectMapper.readValue(ORDER_SHIPPED, OrderEvent.class);

        final Metadata metadata = event.getMetadata();
        Assert.assertEquals("36931a5d-74d6-3033-84ea-a8336197c4ce", metadata.getEid());
        Assert.assertEquals("order_shipped", metadata.getEventType());
        Assert.assertEquals("10410018540147", event.getOrderNumber());

        Assert.assertTrue(event instanceof OrderShippedEvent);
    }

    @Test
    public void shouldDeserializeOrderCreated() throws IOException {
        final OrderEvent event = objectMapper.readValue(ORDER_CREATED, OrderEvent.class);

        final Metadata metadata = event.getMetadata();
        Assert.assertEquals("a3e25946-5ae9-3964-91fa-26ecb7588d67", metadata.getEid());
        Assert.assertEquals("order_created", metadata.getEventType());
        Assert.assertEquals("10410018540147", event.getOrderNumber());

        Assert.assertTrue(event instanceof OrderCreatedEvent);
        Assert.assertEquals("123456", ((OrderCreatedEvent)event).getCustomerNumber());
        Assert.assertEquals("INVOICE", ((OrderCreatedEvent)event).getPaymentMethod());
    }
}
