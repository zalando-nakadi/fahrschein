package org.zalando.fahrschein.e2e;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.zalando.fahrschein.EventAlreadyProcessedException;
import org.zalando.fahrschein.EventPublisher;
import org.zalando.fahrschein.IdentityAcceptEncodingRequestFactory;
import org.zalando.fahrschein.Listener;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.fahrschein.StreamBuilder;
import org.zalando.fahrschein.StreamParameters;
import org.zalando.fahrschein.domain.Metadata;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.http.apache.HttpComponentsRequestFactory;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.jdk11.JavaNetRequestFactory;
import org.zalando.fahrschein.http.simple.SimpleRequestFactory;
import org.zalando.fahrschein.http.spring.SpringRequestFactory;
import org.zalando.fahrschein.opentelemetry.InstrumentedEventListener;
import org.zalando.fahrschein.opentelemetry.InstrumentedEventPublisher;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.runners.Parameterized.Parameters;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;

/*
 * Enable wire-debug by running with -Djdk.httpclient.HttpClient.log=requests
 */
public class NakadiClientEnd2EndTest extends NakadiTestWithDockerCompose {

    private static final Logger logger = LoggerFactory.getLogger("okhttp3.wire");
    private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(logger::debug);

    private static final OpenTelemetry otel = OpenTelemetrySdk.builder().buildAndRegisterGlobal();

    static {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
    }

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // After a Nakadi-Docker upgrade, check if the response metadata changed
        // by enabling deserialization failure on unknown properties.
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    private NakadiClient setUpNakadiSubscriber(RequestFactory requestFactory) {
        return NakadiClient
                .builder(getNakadiUrl(), requestFactory)
                .withObjectMapper(objectMapper)
                .build();
    }

    private EventPublisher setUpNakadiPublisher(RequestFactory requestFactory) {

        NakadiClient client = NakadiClient
                .builder(getNakadiUrl(), requestFactory)
                .withObjectMapper(objectMapper)
                .build();
        return new InstrumentedEventPublisher(client, otel.getTracer("test", "0.0"));
    }

    @Parameters(name = "{1}")
    public static Collection<Object[]> getRequestFactories() {
        List<Function<RequestFactory, RequestFactory>> wrappers = List.of(Function.identity(), a -> new IdentityAcceptEncodingRequestFactory(a));
        List<Function<ContentEncoding, RequestFactory>> factoryProviders = List.of(
                NakadiClientEnd2EndTest::apache,
                NakadiClientEnd2EndTest::spring,
                NakadiClientEnd2EndTest::simple,
                NakadiClientEnd2EndTest::jdk11);
        List<Object[]> parameters = new ArrayList<>();
        for (ContentEncoding e : ContentEncoding.values()) {
            wrappers.forEach(wrapper ->
                    factoryProviders.forEach(factoryProvider ->
                            {
                                RequestFactory rf = factoryProvider.apply(e);
                                RequestFactory wrapped = wrapper.apply(rf);
                                String testName = String.format(Locale.ENGLISH, "%s (%s/%s)", rf.getClass().getSimpleName(), e.name(), wrapped.getClass().getSimpleName());
                                parameters.add(new Object[]{wrapped, testName});
                            }
                    )
            );
        }
        return parameters;
    }

    private static RequestFactory jdk11(ContentEncoding contentEncoding) {
        return new JavaNetRequestFactory(HttpClient.newHttpClient(), Optional.empty(), contentEncoding);
    }

    private static RequestFactory apache(ContentEncoding encoding) {
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        return new HttpComponentsRequestFactory(httpClient, encoding);
    }

    private static RequestFactory spring(ContentEncoding encoding) {
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        final OkHttp3ClientHttpRequestFactory clientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory(client);
        return new SpringRequestFactory(clientHttpRequestFactory, encoding);

    }

    private static RequestFactory simple(ContentEncoding contentEncoding) {
        return new SimpleRequestFactory(contentEncoding);
    }

    @ParameterizedTest
    @MethodSource("getRequestFactories")
    public void testPublish(RequestFactory requestFactory) throws IOException {
        EventPublisher nakadiClient = setUpNakadiPublisher(requestFactory);
        publish(nakadiClient, UUID.randomUUID().toString());
    }

    private List<OrderEvent> publish(EventPublisher nakadiClient, String testId) throws IOException {
        createEventTypes("/eventtypes", testId);
        List<OrderEvent> events = IntStream.range(0, 10)
                .mapToObj(
                        i -> new OrderEvent(new Metadata(testId, OffsetDateTime.now(ZoneOffset.UTC)), testId))
                .collect(toList());
        nakadiClient.publish("fahrschein.e2e-test.ordernumber" + testId, events);
        return events;
    }

    @ParameterizedTest
    @MethodSource("getRequestFactories")
    public void testSubscribe(RequestFactory requestFactory) throws IOException, EventAlreadyProcessedException {
        NakadiClient nakadiClient = setUpNakadiSubscriber(requestFactory);
        String testId = UUID.randomUUID().toString();
        createEventTypes("/eventtypes", testId);
        final Listener<OrderEvent> listener = subscriptionListener();
        final Subscription subscription = nakadiClient
                .subscription("fahrschein-demo", "fahrschein.e2e-test.ordernumber" + testId)
                .withConsumerGroup(testId)
                .readFromBegin()
                .subscribe();
        StreamBuilder b = nakadiClient.stream(subscription)
                .withObjectMapper(objectMapper)
                .withStreamParameters(new StreamParameters()
                        .withBatchFlushTimeout(1)
                        .withStreamLimit(1)
                );
        Executors.newSingleThreadExecutor().submit(
                () -> {
                    try {
                        b.listen(OrderEvent.class, listener);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        EventPublisher eventPublisher = setUpNakadiPublisher(requestFactory);
        List<String> eventOrderNumbers = publish(eventPublisher, testId).stream().map(e -> e.orderNumber).collect(toList());
        // verifies that every order number that was published got consumed
        for (String on : eventOrderNumbers) {
            Mockito.verify(listener, timeout(10000).atLeastOnce()).accept(
                    argThat(streamedEvents ->
                            streamedEvents.stream().map(e -> e.orderNumber).collect(toList()).contains(on)));
        }
    }

    public Listener<OrderEvent> subscriptionListener() {
        InstrumentedEventListener inl = new InstrumentedEventListener(otel.getTracer("test"), "consume");

        // no, you cannot replace this listener with a lambda, because Mockito.spy will complain.
        return Mockito.spy(new Listener<OrderEvent>() {
            @Override
            public void accept(List<OrderEvent> events) {
                events.forEach(e -> inl.accept(e, orderEvent -> {
                    // here goes your business logic...
                }));
            }
        });
    }
}
