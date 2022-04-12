package org.zalando.fahrschein.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.zalando.fahrschein.e2e.NakadiTestWithDockerCompose;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zalando.fahrschein.*;
import org.zalando.fahrschein.domain.Metadata;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.http.apache.HttpComponentsRequestFactory;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.jdk11.JavaNetRequestFactory;
import org.zalando.fahrschein.http.simple.SimpleRequestFactory;
import org.zalando.fahrschein.http.spring.SpringRequestFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.runners.Parameterized.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/*
 * Enable wire-debug by running with -Djdk.httpclient.HttpClient.log=requests
 */
@RunWith(Parameterized.class)
public class RequestFactoryTest extends NakadiTestWithDockerCompose {

    private static final Logger logger = LoggerFactory.getLogger("okhttp3.wire");
    private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(logger::debug);

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

    public final RequestFactory requestFactory;

    private NakadiClient nakadiClient;

    public RequestFactoryTest(RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    @Before
    public void setUpNakadiClient() {
        nakadiClient = NakadiClient
                .builder(getNakadiUrl(), requestFactory)
                .withObjectMapper(objectMapper)
                .build();
    }

    @Parameters
    public static Collection<Object[]> getRequestFactories() {
        List<Function<RequestFactory, RequestFactory>> wrappers = List.of(a -> a, a -> new IdentityAcceptEncodingRequestFactory(a));
        List<Function<ContentEncoding, RequestFactory>> factoryProviders = List.of(
                RequestFactoryTest::apache,
                RequestFactoryTest::spring,
                RequestFactoryTest::simple,
                RequestFactoryTest::jdk11);
        List<Object[]> parameters = new ArrayList<>();
        for (ContentEncoding e : ContentEncoding.values()) {
            wrappers.forEach(wrapper ->
                    factoryProviders.forEach(factoryProvider ->
                            parameters.add(new Object[]{wrapper.apply(factoryProvider.apply(e))})
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
        Mockito.verify(listener, timeout(10000).atLeastOnce()).accept(anyList());
    }

    public Listener<OrderEvent> subscriptionListener() {
        return Mockito.mock(Listener.class);
    }
}
