package org.zalando.fahrschein.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import e2e.NakadiTestWithDockerCompose;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zalando.fahrschein.*;
import org.zalando.fahrschein.domain.Metadata;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public abstract class AbstractRequestFactoryTest extends NakadiTestWithDockerCompose {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private NakadiClient nakadiClient;

    @Before
    public void setUpNakadiClient() {
        nakadiClient = NakadiClient
                .builder(getNakadiUrl())
                .withObjectMapper(objectMapper)
                .withRequestFactory(getRequestFactory())
                .build();
    }

    protected abstract RequestFactory getRequestFactory();

    @Before
    public void createEventTypes() throws IOException {
        createEventTypes("/eventtypes");
    }

    @Test
    public void testPublish() throws IOException {
        nakadiClient.publish("fahrschein.e2e-test.ordernumber",
                IntStream.range(0, 10)
                        .mapToObj(
                                i -> new OrderEvent(new Metadata(UUID.randomUUID().toString(), OffsetDateTime.now()), "ABC-" + i))
                        .collect(Collectors.toList()));
    }

    @Test
    public void testSubscribe() throws IOException, EventAlreadyProcessedException {
        final Listener<OrderEvent> listener = subscriptionListener();
        final Subscription subscription = nakadiClient.subscription("fahrschein-demo", "fahrschein.e2e-test.ordernumber")
                .withConsumerGroup(UUID.randomUUID().toString())
                .readFromBegin()
                .subscribe();
        StreamBuilder b = nakadiClient.stream(subscription)
                .withObjectMapper(objectMapper)
                .withStreamParameters(new StreamParameters()
                        .withBatchFlushTimeout(1)
                        .withStreamLimit(1)
                );
        Executors.newSingleThreadExecutor().submit(
                (() -> {
                    try {
                        b.listen(OrderEvent.class, listener);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }));
        testPublish();
        Mockito.verify(listener, timeout(10000).times(1)).accept(anyList());
    }

    public Listener<OrderEvent> subscriptionListener() {
        return Mockito.mock(Listener.class, withSettings().verboseLogging());
    }
}
