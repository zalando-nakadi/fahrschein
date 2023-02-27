package org.zalando.fahrschein.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.fahrschein.PlatformAccessTokenProvider;
import org.zalando.fahrschein.domain.Metadata;
import org.zalando.fahrschein.example.domain.MultiEventProcessor;
import org.zalando.fahrschein.example.domain.OrderCreatedEvent;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.simple.SimpleRequestFactory;
import org.zalando.fahrschein.opentelemetry.OpenTelemetryHelper;
import org.zalando.jackson.datatype.money.MoneyModule;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProducerExample {

    private static final Logger LOG = LoggerFactory.getLogger(MultiEventProcessor.class);
    private static final URI NAKADI_URI = URI.create("http://localhost:8080");
    public static final String ORDER_CREATED = "order_created";

    private static Tracer tracer = OpenTelemetrySetup.init();


    public static void main(String[] args) throws Exception {
        produceEvents();
    }

    private static ObjectMapper getObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new MoneyModule());
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    private static void produceEvents() throws IOException, InterruptedException {

        final NakadiClient nakadiClient = NakadiClient
                .builder(NAKADI_URI, new SimpleRequestFactory(ContentEncoding.IDENTITY))
                .withObjectMapper(getObjectMapper())
                .withAccessTokenProvider(new PlatformAccessTokenProvider(Paths.get("./fahrschein-example/src/main/resources/meta/credentials"),"nakadi")).build();

        while (true) {
            Span span = tracer.spanBuilder("publish_orders").startSpan();
            try (Scope __ = span.makeCurrent()) {
                    Map<String, String> carrierContext = OpenTelemetryHelper.currentContextToMap();
                    List<OrderCreatedEvent> events = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        Metadata metadata = new Metadata(UUID.randomUUID().toString(), OffsetDateTime.now(ZoneOffset.UTC), "sample-flow-id", carrierContext);
                        events.add(new OrderCreatedEvent(metadata, "123", Money.of(123, "EUR"), "1234", "paypal"));
                    }
                    LOG.info("publishing {} events", events.size());
                    nakadiClient.publish(ORDER_CREATED, events);
            } finally {
                span.end();
            }
            Thread.sleep(1000l);
        }

    }

}
