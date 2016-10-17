package org.zalando.fahrschein.salesorder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.zalando.fahrschein.AccessTokenProvider;
import org.zalando.fahrschein.AuthorizedClientHttpRequestFactory;
import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.EventProcessingException;
import org.zalando.fahrschein.ExponentialBackoffStrategy;
import org.zalando.fahrschein.InMemoryCursorManager;
import org.zalando.fahrschein.InMemoryPartitionManager;
import org.zalando.fahrschein.Listener;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.fahrschein.ProblemHandlingClientHttpRequestFactory;
import org.zalando.fahrschein.ZignAccessTokenProvider;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.salesorder.domain.SalesOrderPlaced;
import org.zalando.jackson.datatype.money.MoneyModule;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        final URI baseUri = URI.create("https://nakadi-sandbox-hila.aruha-test.zalan.do");
        final String eventName = "sales-order-service.order-placed";

        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new MoneyModule());
        objectMapper.registerModule(new GuavaModule());
        objectMapper.registerModule(new ParameterNamesModule());

        final Listener<SalesOrderPlaced> listener = events -> {
            if (Math.random() < 0.01) {
                // For testing reconnection logic
                throw new EventProcessingException("Random failure");
            } else {
                for (SalesOrderPlaced salesOrderPlaced : events) {
                    LOG.info("Received sales order [{}]", salesOrderPlaced.getSalesOrder().getOrderNumber());
                }
            }
        };


        /*
        HikariConfig hikariConfig = new HikariConfig();
        //hikariConfig.setJdbcUrl("jdbc:h2:file:./nakadi-cursor.db;MODE=PostgreSQL");
        hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/local_nakadi_cursor_db");
        hikariConfig.setUsername("postgres");
        hikariConfig.setPassword("postgres");

        final DataSource dataSource = new HikariDataSource(hikariConfig);

        final CursorManager cursorManager = new PersistentCursorManager(dataSource);
        */

        final AccessTokenProvider tokenProvider = new ZignAccessTokenProvider();

        final SimpleClientHttpRequestFactory requestFactoryDelegate = new SimpleClientHttpRequestFactory();
        requestFactoryDelegate.setConnectTimeout(400);
        requestFactoryDelegate.setReadTimeout(60*1000);
        final ClientHttpRequestFactory requestFactory = new AuthorizedClientHttpRequestFactory(
                new ProblemHandlingClientHttpRequestFactory(requestFactoryDelegate), tokenProvider);

        final CursorManager cursorManager = new InMemoryCursorManager();
        //final ManagedCursorManager cursorManager = new ManagedCursorManager(baseUri, requestFactory, objectMapper);

        final InMemoryPartitionManager partitionManager = new InMemoryPartitionManager();

        final ExponentialBackoffStrategy exponentialBackoffStrategy = new ExponentialBackoffStrategy();

        final NakadiClient nakadiClient = new NakadiClient(baseUri, requestFactory, exponentialBackoffStrategy, objectMapper, cursorManager);

        final List<Partition> partitions = nakadiClient.getPartitions(eventName);

        for (Partition partition : partitions) {
            LOG.info("Partition [{}] has oldest offset [{}] and newest offset [{}]", partition.getPartition(), partition.getOldestAvailableOffset(), partition.getNewestAvailableOffset());
            cursorManager.onSuccess(eventName, new Cursor(partition.getPartition(), "BEGIN"));
        }

        nakadiClient.prepareListening(eventName, SalesOrderPlaced.class, listener).startListening();
        /*
        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);



        final IORunnable runnable = () -> {
            final List<LockedPartition> lockedPartitions = partitionManager.lockPartitions("fahrschein-demo", eventName, partitions, Thread.currentThread().getName(), 30, TimeUnit.SECONDS);

            if (!lockedPartitions.isEmpty()) {

                final StreamParameters streamParameters = new StreamParameters().withStreamTimeout(5 * 60 * 1000);

                //final Subscription subscription = nakadiClient.subscribe("fahrschein-demo2", eventName, "fahrschein-demo-sales-order-placed");
                //nakadiClient.listen(subscription, SalesOrderPlaced.class, listener, streamParameters);

                nakadiClient.listen(eventName, SalesOrderPlaced.class, listener, streamParameters, lockedPartitions);
            }
        };

        scheduledExecutorService.scheduleWithFixedDelay(runnable.unchecked(), 0, 10, TimeUnit.SECONDS);
        */

    }
}
