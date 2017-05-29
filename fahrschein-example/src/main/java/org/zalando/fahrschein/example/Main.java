package org.zalando.fahrschein.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.*;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.example.domain.OrderEvent;
import org.zalando.fahrschein.example.domain.SalesOrder;
import org.zalando.fahrschein.example.domain.SalesOrderPlaced;
import org.zalando.fahrschein.http.apache.HttpComponentsClientHttpRequestFactory;
import org.zalando.fahrschein.inmemory.InMemoryCursorManager;
import org.zalando.fahrschein.jdbc.JdbcCursorManager;
import org.zalando.fahrschein.jdbc.JdbcPartitionManager;
import org.zalando.jackson.datatype.money.MoneyModule;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String SALES_ORDER_SERVICE_ORDER_PLACED = "sales-order-service.order-placed";
    private static final URI NAKADI_URI = URI.create("https://nakadi-staging.aruha-test.zalan.do");
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/local_nakadi_cursor_db";
    private static final String JDBC_USERNAME = "postgres";
    private static final String JDBC_PASSWORD = "postgres";
    public static final String ORDER_CREATED = "eventlog.e96001_order_created";
    public static final String ORDER_PAYMENT_STATUS_ACCEPTED = "eventlog.e62001_order_payment_status_accepted";

    public static void main(String[] args) throws IOException, InterruptedException {

        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new MoneyModule());
        objectMapper.registerModule(new ParameterNamesModule());

        final Listener<SalesOrderPlaced> listener = events -> {
            if (Math.random() < 0.0001) {
                // For testing reconnection logic
                throw new EventProcessingException("Random failure");
            } else {
                for (SalesOrderPlaced salesOrderPlaced : events) {
                    final SalesOrder order = salesOrderPlaced.getSalesOrder();
                    LOG.info("Received sales order [{}] created at [{}]", order.getOrderNumber(), order.getCreatedAt());
                }
            }
        };

        //subscriptionListen(objectMapper, listener);

        //subscriptionListenHttpComponents(objectMapper, listener);

        //subscriptionListenWithPositionCursors(objectMapper, listener);

        subscriptionMultipleEvents(objectMapper);

        //simpleListen(objectMapper, listener);

        //persistentListen(objectMapper, listener);

        //multiInstanceListen(objectMapper, listener);
    }

    private static void subscriptionMultipleEvents(ObjectMapper objectMapper) throws IOException {

        final Listener<OrderEvent> listener = events -> {
            for (OrderEvent event: events) {
                event.process();
            }
        };

        final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
                .withAccessTokenProvider(new ZignAccessTokenProvider())
                .build();

        final Set<String> events = new HashSet<>(asList(ORDER_CREATED, ORDER_PAYMENT_STATUS_ACCEPTED));

        final Subscription subscription = nakadiClient.subscription("fahrschein-demo", events)
                .withConsumerGroup("fahrschein-demo")
                .readFromEnd()
                .subscribe();

        nakadiClient.stream(subscription)
                .withObjectMapper(objectMapper)
                .listen(OrderEvent.class, listener);
    }

    private static void subscriptionListenWithPositionCursors(ObjectMapper objectMapper, Listener<SalesOrderPlaced> listener) throws IOException {

        final List<Cursor> cursors = asList(
                new Cursor("0", "000000000000109993", "sales-order-service.order-placed"),
                new Cursor("1", "000000000000110085", "sales-order-service.order-placed"),
                new Cursor("2", "000000000000109128", "sales-order-service.order-placed"),
                new Cursor("3", "000000000000110205", "sales-order-service.order-placed"),
                new Cursor("4", "000000000000109161", "sales-order-service.order-placed"),
                new Cursor("5", "000000000000109087", "sales-order-service.order-placed"),
                new Cursor("6", "000000000000109100", "sales-order-service.order-placed"),
                new Cursor("7", "000000000000109146", "sales-order-service.order-placed"));

        final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
                .withAccessTokenProvider(new ZignAccessTokenProvider())
                .build();

        final Subscription subscription = nakadiClient.subscription("fahrschein-demo", SALES_ORDER_SERVICE_ORDER_PLACED)
                .withConsumerGroup("fahrschein-demo")
                .readFromCursors(cursors)
                .subscribe();

        nakadiClient.stream(subscription)
                .withObjectMapper(objectMapper)
                .listen(SalesOrderPlaced.class, listener);
    }

    private static void subscriptionListen(ObjectMapper objectMapper, Listener<SalesOrderPlaced> listener) throws IOException {

        final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
                .withAccessTokenProvider(new ZignAccessTokenProvider())
                .build();

        final Subscription subscription = nakadiClient.subscription("fahrschein-demo", SALES_ORDER_SERVICE_ORDER_PLACED)
                .withConsumerGroup("fahrschein-demo")
                .readFromEnd()
                .subscribe();

        nakadiClient.stream(subscription)
                .withObjectMapper(objectMapper)
                .listen(SalesOrderPlaced.class, listener);
    }

    private static void subscriptionListenHttpComponents(ObjectMapper objectMapper, Listener<SalesOrderPlaced> listener) throws IOException {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(60000)
                .setConnectTimeout(2000)
                .setConnectionRequestTimeout(8000)
                .setContentCompressionEnabled(false)
                .build();

        final ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setBufferSize(512)
                .build();

        final CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultConnectionConfig(connectionConfig)
                .setConnectionTimeToLive(30, TimeUnit.SECONDS)
                .disableAutomaticRetries()
                .disableRedirectHandling()
                .setMaxConnTotal(8)
                .setMaxConnPerRoute(2)
                .build();

        final ClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
                .withClientHttpRequestFactory(clientHttpRequestFactory)
                .withAccessTokenProvider(new ZignAccessTokenProvider())
                .build();

        final Subscription subscription = nakadiClient.subscription("fahrschein-demo", SALES_ORDER_SERVICE_ORDER_PLACED)
                .withConsumerGroup("fahrschein-demo")
                .readFromEnd()
                .subscribe();

        nakadiClient.stream(subscription)
                .withObjectMapper(objectMapper)
                .listen(SalesOrderPlaced.class, listener);
    }

    private static void simpleListen(ObjectMapper objectMapper, Listener<SalesOrderPlaced> listener) throws IOException {
        final InMemoryCursorManager cursorManager = new InMemoryCursorManager();

        final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
                .withAccessTokenProvider(new ZignAccessTokenProvider())
                .withCursorManager(cursorManager)
                .build();

        final List<Partition> partitions = nakadiClient.getPartitions(SALES_ORDER_SERVICE_ORDER_PLACED);

        nakadiClient.stream(SALES_ORDER_SERVICE_ORDER_PLACED)
                .readFromBegin(partitions)
                .withObjectMapper(objectMapper)
                .withBackoffStrategy(new ExponentialBackoffStrategy().withMaxRetries(10))
                .listen(SalesOrderPlaced.class, listener);
    }

    private static void persistentListen(ObjectMapper objectMapper, Listener<SalesOrderPlaced> listener) throws IOException {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(JDBC_URL);
        hikariConfig.setUsername(JDBC_USERNAME);
        hikariConfig.setPassword(JDBC_PASSWORD);

        final DataSource dataSource = new HikariDataSource(hikariConfig);

        final JdbcCursorManager cursorManager = new JdbcCursorManager(dataSource, "fahrschein-demo");

        final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
                .withAccessTokenProvider(new ZignAccessTokenProvider())
                .withCursorManager(cursorManager)
                .build();

        final List<Partition> partitions = nakadiClient.getPartitions(SALES_ORDER_SERVICE_ORDER_PLACED);

        nakadiClient.stream(SALES_ORDER_SERVICE_ORDER_PLACED)
                .readFromBegin(partitions)
                .withObjectMapper(objectMapper)
                .listen(SalesOrderPlaced.class, listener);
    }

    private static void multiInstanceListen(ObjectMapper objectMapper, Listener<SalesOrderPlaced> listener) throws IOException {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(JDBC_URL);
        hikariConfig.setUsername(JDBC_USERNAME);
        hikariConfig.setPassword(JDBC_PASSWORD);

        final DataSource dataSource = new HikariDataSource(hikariConfig);

        final ZignAccessTokenProvider accessTokenProvider = new ZignAccessTokenProvider();

        final AtomicInteger name = new AtomicInteger();
        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(16);

        for (int i = 0; i < 12; i++) {
            final String instanceName = "consumer-" + name.getAndIncrement();
            final JdbcPartitionManager partitionManager = new JdbcPartitionManager(dataSource, "fahrschein-demo");
            final JdbcCursorManager cursorManager = new JdbcCursorManager(dataSource, "fahrschein-demo");

            final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
                    .withAccessTokenProvider(accessTokenProvider)
                    .withCursorManager(cursorManager)
                    .build();

            final List<Partition> partitions = nakadiClient.getPartitions(SALES_ORDER_SERVICE_ORDER_PLACED);

            final IORunnable instance = () -> {

                final IORunnable runnable = () -> {
                    final Optional<Lock> optionalLock = partitionManager.lockPartitions(SALES_ORDER_SERVICE_ORDER_PLACED, partitions, instanceName);

                    if (optionalLock.isPresent()) {
                        final Lock lock = optionalLock.get();
                        try {
                            nakadiClient.stream(SALES_ORDER_SERVICE_ORDER_PLACED)
                                    .withLock(lock)
                                    .withObjectMapper(objectMapper)
                                    .withStreamParameters(new StreamParameters().withStreamLimit(10))
                                    .withBackoffStrategy(new NoBackoffStrategy())
                                    .listen(SalesOrderPlaced.class, listener);
                        } finally {
                            partitionManager.unlockPartitions(lock);
                        }
                    }
                };

                scheduledExecutorService.scheduleWithFixedDelay(runnable.unchecked(), 0, 1, TimeUnit.SECONDS);
            };
            scheduledExecutorService.submit(instance.unchecked());
        }

        try {
            Thread.sleep(60L*1000);
            scheduledExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
