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
import org.zalando.fahrschein.EventProcessingException;
import org.zalando.fahrschein.ExponentialBackoffException;
import org.zalando.fahrschein.ExponentialBackoffStrategy;
import org.zalando.fahrschein.Listener;
import org.zalando.fahrschein.ManagedCursorManager;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.fahrschein.ProblemHandlingClientHttpRequestFactory;
import org.zalando.fahrschein.StreamParameters;
import org.zalando.fahrschein.ZignAccessTokenProvider;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.salesorder.domain.SalesOrderPlaced;
import org.zalando.jackson.datatype.money.MoneyModule;
import org.zalando.problem.ProblemModule;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException, ExponentialBackoffException {
        final URI baseUri = new URI("https://nakadi-sandbox-24.aruha-test.zalan.do");
        final String eventName = "sales-order-service.order-placed";

        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new MoneyModule());
        objectMapper.registerModule(new ProblemModule());
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

        //final CursorManager cursorManager = new InMemoryCursorManager();

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
        final ClientHttpRequestFactory requestFactory = new AuthorizedClientHttpRequestFactory(new ProblemHandlingClientHttpRequestFactory(requestFactoryDelegate, objectMapper), tokenProvider);

        final ManagedCursorManager cursorManager = new ManagedCursorManager(baseUri, requestFactory, objectMapper);

        final ExponentialBackoffStrategy exponentialBackoffStrategy = new ExponentialBackoffStrategy();

        final NakadiClient nakadiClient = new NakadiClient(baseUri, requestFactory, exponentialBackoffStrategy, objectMapper, cursorManager);

        /*
        final List<Partition> partitions = nakadiClient.getPartitions(eventName);

        for (Partition partition : partitions) {
            LOG.info("Partition [{}] has oldest offset [{}] and newest offset [{}]", partition.getPartition(), partition.getOldestAvailableOffset(), partition.getNewestAvailableOffset());
        }

        */
        //cursorManager.fromOldestAvailableOffset(eventName, partitions);


        final Subscription subscription = nakadiClient.subscribe("fahrschein-demo", eventName, "fahrschein-demo-sales-order-placed");

        //nakadiClient.listen(eventName, SalesOrderPlaced.class, listener, new StreamParameters().withStreamTimeout(5 * 60));
        nakadiClient.listen(subscription, SalesOrderPlaced.class, listener, new StreamParameters().withStreamTimeout(5 * 60));
    }
}
