package org.zalando.fahrschein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.zalando.fahrschein.domain.AbstractDataChangeEvent;
import org.zalando.fahrschein.domain.DataChangeEvent;
import org.zalando.fahrschein.domain.DataOperation;
import org.zalando.fahrschein.domain.Event;
import org.zalando.fahrschein.domain.Metadata;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NakadiReaderDeserializationTest {
    private final URI uri = java.net.URI.create("http://example.com/events");
    private final ObjectMapper objectMapper = createObjectMapper();
    private final CursorManager cursorManager = mock(CursorManager.class);
    private final RequestFactory requestFactory = mock(RequestFactory.class);
    private final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();

    private static ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new ParameterNamesModule());

        objectMapper.registerSubtypes(CustomerChanged.class);

        return objectMapper;
    }

    public static class SalesOrder {
        private final String orderNumber;

        @JsonCreator
        public SalesOrder(@JsonProperty("order_number") String orderNumber) {
            this.orderNumber = orderNumber;
        }

        public String getOrderNumber() {
            return orderNumber;
        }
    }

    public static class SalesOrderPlaced implements Event {
        private final Metadata metadata;
        private final SalesOrder salesOrder;

        @JsonCreator
        public SalesOrderPlaced(@JsonProperty("metadata") Metadata metadata,
                                @JsonProperty("sales_order") SalesOrder salesOrder) {
            this.metadata = metadata;
            this.salesOrder = salesOrder;
        }

        @Override
        public Metadata getMetadata() {
            return metadata;
        }

        public SalesOrder getSalesOrder() {
            return salesOrder;
        }
    }

    public static class Customer {
        private final String customerNumber;
        private final String name;

        @JsonCreator
        public Customer(@JsonProperty("customer_number") String customerNumber,
                        @JsonProperty("name")String name) {
            this.customerNumber = customerNumber;
            this.name = name;
        }

        public String getCustomerNumber() {
            return customerNumber;
        }

        public String getName() {
            return name;
        }
    }

    @JsonTypeName(CustomerChanged.DATA_TYPE)
    public static class CustomerChanged extends AbstractDataChangeEvent<Customer> {
        public static final String DATA_TYPE = "customer";

        @JsonCreator
        public CustomerChanged(@JsonProperty("metadata") Metadata metadata, @JsonProperty("data_type") String dataType, @JsonProperty("data_op") DataOperation dataOp, @JsonProperty("data") Customer data) {
            super(metadata, dataType, dataOp, data);
        }

    }

    @JsonTypeName(SalesOrderChanged.DATA_TYPE)
    public static class SalesOrderChanged extends AbstractDataChangeEvent<SalesOrder> {
        public static final String DATA_TYPE = "salesOrder";

        @JsonCreator
        public SalesOrderChanged(@JsonProperty("metadata") Metadata metadata, @JsonProperty("data_type") String dataType, @JsonProperty("data_op") DataOperation dataOp, @JsonProperty("data") SalesOrder data) {
            super(metadata, dataType, dataOp, data);
        }
    }

    public static class NodeChangedEvent extends AbstractDataChangeEvent<JsonNode> {

        @JsonCreator
        public NodeChangedEvent(@JsonProperty("metadata") Metadata metadata, @JsonProperty("data_type") String dataType, @JsonProperty("data_op") DataOperation dataOp, @JsonProperty("data") JsonNode data) {
            super(metadata, dataType, dataOp, data);
        }
    }

    private void setupResponse(int partition, int offset, final String data) throws IOException {
        final String body = String.format("{\"cursor\":{\"partition\":\"%d\",\"offset\":\"%d\"},\"events\":[%s]}", partition, offset, data);
        final Response response = mock(Response.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream(body.getBytes("utf-8"));
        when(response.getBody()).thenReturn(initialInputStream);

        final Request request = mock(Request.class);
        when(request.execute()).thenReturn(response);

        when(requestFactory.createRequest(uri, "GET")).thenReturn(request);
    }

    private <T> List<T> readSingleBatch(String eventName, Class<T> eventClass) throws IOException {
        final List<T> result = new ArrayList<>();
        final NakadiReader<T> nakadiReader = new NakadiReader<T>(uri, requestFactory, backoffStrategy, cursorManager, objectMapper,
                Collections.singleton(eventName), Optional.empty(), Optional.empty(), eventClass, result::addAll);
        nakadiReader.readSingleBatch();

        return result;
    }

    @Test
    public void shouldDeserializeBusinessEvent() throws IOException {
        setupResponse(1, 1, "{\"sales_order\":{\"order_number\":\"1234\"}}");

        final List<SalesOrderPlaced> events = readSingleBatch("sales-salesOrder-placed", SalesOrderPlaced.class);

        assertThat(events, Matchers.notNullValue());
        assertThat(events, hasSize(1));
        final SalesOrderPlaced event = events.get(0);
        final SalesOrder salesOrder = event.getSalesOrder();
        assertThat(salesOrder.orderNumber, Matchers.equalTo("1234"));
        assertThat(event.getMetadata(), Matchers.nullValue());
    }

    @Test
    public void shouldDeserializeBusinessEventMetadata() throws IOException {
        setupResponse(1, 1, "{\"metadata\":{\"eid\":\"5678\",\"occurred_at\":\"2016-10-26T19:20:21.123Z\",\"received_at\":\"2016-10-26T20:21:22+01:00\",\"flow_id\":\"ABCD\"},\"sales_order\":{\"order_number\":\"1234\"}}");

        final List<SalesOrderPlaced> events = readSingleBatch("sales-salesOrder-placed", SalesOrderPlaced.class);

        assertThat(events, Matchers.notNullValue());
        assertThat(events, hasSize(1));
        final SalesOrderPlaced salesOrderPlaced = events.get(0);
        final SalesOrder salesOrder = salesOrderPlaced.getSalesOrder();
        assertThat(salesOrder.getOrderNumber(), Matchers.equalTo("1234"));
        final Metadata metadata = salesOrderPlaced.getMetadata();
        assertThat(metadata, Matchers.notNullValue());
        assertThat(metadata.getEid(), Matchers.equalTo("5678"));
        assertThat(metadata.getFlowId(), Matchers.equalTo("ABCD"));
        assertThat(metadata.getOccurredAt(), Matchers.equalTo(OffsetDateTime.of(2016, 10, 26, 19, 20, 21, 123_000_000, ZoneOffset.UTC)));
        assertThat(metadata.getReceivedAt(), Matchers.equalTo(OffsetDateTime.of(2016, 10, 26, 20, 21, 22, 0, ZoneOffset.ofHours(1))));
        assertThat(metadata.getSpanCtx(), Matchers.equalTo(Collections.emptyMap()));
    }

    @Test
    public void shouldDeserializeSpanCtxInBusinessEventMetadata() throws IOException {
        setupResponse(1, 1, "{\"metadata\":{\"eid\":\"5678\",\"occurred_at\":\"2016-10-26T19:20:21.123Z\",\"received_at\":\"2016-10-26T20:21:22+01:00\",\"flow_id\":\"ABCD\",\"span_ctx\":{\"ot-tracer-spanid\":\"78cc5b6e96e8a5a2\",\"ot-tracer-traceid\":\"9df69e766320993f\",\"ot-tracer-sampled\":\"true\"}},\"sales_order\":{\"order_number\":\"1234\"}}");

        final List<SalesOrderPlaced> events = readSingleBatch("sales-salesOrder-placed", SalesOrderPlaced.class);

        assertThat(events, Matchers.notNullValue());
        assertThat(events, hasSize(1));
        final SalesOrderPlaced salesOrderPlaced = events.get(0);
        final SalesOrder salesOrder = salesOrderPlaced.getSalesOrder();
        final Metadata metadata = salesOrderPlaced.getMetadata();

        assertThat(metadata.getSpanCtx(), Matchers.hasEntry("ot-tracer-spanid", "78cc5b6e96e8a5a2"));
        assertThat(metadata.getSpanCtx(), Matchers.hasEntry("ot-tracer-traceid", "9df69e766320993f"));
        assertThat(metadata.getSpanCtx(), Matchers.hasEntry("ot-tracer-sampled", "true"));
    }

    @Test
    public void shouldDeserializeDataChangeEvent() throws IOException {
        setupResponse(1, 1, "{\"data_type\":\"customer\",\"data_op\":\"C\",\"data\":{\"customer_number\":\"1234\",\"name\":\"Test\"}}");

        final List<CustomerChanged> events = readSingleBatch("customer", CustomerChanged.class);

        assertThat(events, Matchers.notNullValue());
        assertThat(events, hasSize(1));

        final DataChangeEvent<Customer> event = events.get(0);
        assertThat(event.getDataType(), Matchers.equalTo("customer"));
        assertThat(event.getDataOp(), Matchers.equalTo(DataOperation.CREATE));

        final Customer customer = event.getData();
        assertThat(customer, Matchers.notNullValue());
        assertThat(customer.getCustomerNumber(), Matchers.equalTo("1234"));
        assertThat(customer.getName(), Matchers.equalTo("Test"));
    }


    @Test
    public void shouldDeserializeDataChangeEventBasedOnDataType() throws IOException {
        setupResponse(1, 1, "{\"data_type\":\"customer\",\"data_op\":\"C\",\"data\":{\"customer_number\":\"1234\",\"name\":\"Test\"}}");

        final List<CustomerChanged> events = readSingleBatch("customer", CustomerChanged.class);

        assertThat(events, Matchers.notNullValue());
        assertThat(events, hasSize(1));

        final DataChangeEvent<?> event = events.get(0);
        assertThat(event.getDataType(), Matchers.equalTo("customer"));
        assertThat(event.getDataOp(), Matchers.equalTo(DataOperation.CREATE));

        final Object data = event.getData();
        assertThat(data, Matchers.instanceOf(Customer.class));

        final Customer customer = (Customer) data;
        assertThat(customer, Matchers.notNullValue());
        assertThat(customer.getCustomerNumber(), Matchers.equalTo("1234"));
        assertThat(customer.getName(), Matchers.equalTo("Test"));
    }

    @Test
    public void shouldDeserializeDataChangeEventToJsonNode() throws IOException {
        setupResponse(1, 1, "{\"data_type\":\"customer\",\"data_op\":\"C\",\"data\":{\"customer_number\":\"1234\",\"name\":\"Test\"}}");

        final List<NodeChangedEvent> events = readSingleBatch("customer", NodeChangedEvent.class);

        assertThat(events, Matchers.notNullValue());
        assertThat(events, hasSize(1));

        final DataChangeEvent<JsonNode> event = events.get(0);
        assertThat(event.getDataType(), Matchers.equalTo("customer"));
        assertThat(event.getDataOp(), Matchers.equalTo(DataOperation.CREATE));

        final JsonNode customer = event.getData();
        assertThat(customer, Matchers.notNullValue());
        assertThat(customer.get("customer_number").asText(), Matchers.equalTo("1234"));
        assertThat(customer.get("name").asText(), Matchers.equalTo("Test"));
    }


}
