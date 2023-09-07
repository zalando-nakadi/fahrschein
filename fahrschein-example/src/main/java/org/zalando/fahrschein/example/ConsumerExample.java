package org.zalando.fahrschein.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import org.zalando.fahrschein.ExponentialBackoffStrategy;
import org.zalando.fahrschein.Listener;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.fahrschein.PlatformAccessTokenProvider;
import org.zalando.fahrschein.StreamParameters;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.example.domain.MultiEventProcessor;
import org.zalando.fahrschein.example.domain.OrderEvent;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.simple.SimpleRequestFactory;
import org.zalando.fahrschein.metrics.micrometer.MicrometerMetricsCollector;
import org.zalando.jackson.datatype.money.MoneyModule;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.zalando.fahrschein.AuthorizationBuilder.authorization;
import static org.zalando.fahrschein.domain.Authorization.AuthorizationAttribute.ANYONE;

public class ConsumerExample {

    private static final URI NAKADI_URI = URI.create("http://localhost:8080");
    public static final String ORDER_CREATED = "order_created";
    public static final String ORDER_PAYMENT_STATUS_ACCEPTED = "payment_accepted";

    private static Tracer tracer = OpenTelemetrySetup.init();

    public static void main(String[] args) throws IOException {
        subscriptionToMultipleEvents();
    }

    private static ObjectMapper getObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new MoneyModule());
        return objectMapper;
    }

    private static void subscriptionToMultipleEvents() throws IOException {
        final ObjectMapper objectMapper = getObjectMapper();
        final MultiEventProcessor processor = new MultiEventProcessor(tracer);

        final Listener<OrderEvent> listener = events -> {
            for (OrderEvent event : events) {
                event.process(processor);
            }
        };

        final NakadiClient nakadiClient = NakadiClient
                .builder(NAKADI_URI, new SimpleRequestFactory(ContentEncoding.IDENTITY))
                .withAccessTokenProvider(new PlatformAccessTokenProvider(Paths.get("./fahrschein-example/src/main/resources/meta/credentials"),"nakadi")).build();

        final Set<String> events = new HashSet<>(asList(ORDER_CREATED, ORDER_PAYMENT_STATUS_ACCEPTED));

        StreamParameters streamParameters = new StreamParameters()
                .withBatchLimit(2)
                .withBatchFlushTimeout(10)
                .withStreamTimeout(10)
                .withMaxUncommittedEvents(10);

        final Subscription subscription = nakadiClient
                .subscription("fahrschein-demo", events)
                .withConsumerGroup("fahrschein-demo")
                .withAuthorization(authorization().addAdmin("user", "you")
                        .addAdmin("user", "your_friend")
                        .addAdmin("user", "your_dog")
                        .withReaders(ANYONE).build())
                .readFromBegin()
                .subscribe();

        MeterRegistry registry = new SimpleMeterRegistry();

        nakadiClient.stream(subscription)
                .withObjectMapper(objectMapper)
                .withMetricsCollector(new MicrometerMetricsCollector(registry))
                .withStreamParameters(streamParameters)
                .withBackoffStrategy(new ExponentialBackoffStrategy())
                .listen(OrderEvent.class, listener);
    }

}
