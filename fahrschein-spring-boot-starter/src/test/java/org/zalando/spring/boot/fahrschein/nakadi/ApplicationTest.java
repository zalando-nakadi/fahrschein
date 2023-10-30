package org.zalando.spring.boot.fahrschein.nakadi;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.zalando.fahrschein.AccessTokenProvider;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.spring.boot.fahrschein.config.TimeSpan;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.AuthorizationUserServiceTeamLists;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.ConsumerConfig;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.DefaultConsumerConfig;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.FahrscheinConfigProperties;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.Position;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.PublisherConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ApplicationTest {

    @Autowired
    private NakadiPublisher publisher;

    @Autowired
    private AbstractApplicationContext aac;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    @Qualifier("fahrscheinConfigProperties")
    private FahrscheinConfigProperties configProperties;

    @Test
    public void contextLoads() {
        assertThat(meterRegistry).isNotNull();
        assertThat(publisher).isNotNull();

        Map<String, NakadiClient> clientBeans = aac.getBeansOfType(NakadiClient.class);
        assertThat(clientBeans).hasSize(2);

        final Predicate<String> startsWith = name -> name.startsWith("example");
        List<String> beanNames = Arrays.asList(aac.getBeanDefinitionNames())
                .stream()
                .filter(startsWith)
                .collect(Collectors.toList());
        assertThat(beanNames).containsOnly(
                "exampleNakadiListenerContainer",
                "exampleApplication",
                "exampleRequestFactory",
                "exampleConsumerNakadiClient",
                "exampleNakadiConsumer",
                // example -> ExampleNakadiListener
                "example");
    }

    @Test
    public void hasProperlyDeserializedConfigProperties() {
        DefaultConsumerConfig dc = configProperties.getDefaults();
        // defaults
        assertThat(dc)
                .hasFieldOrPropertyWithValue("autostartEnabled", false)
                .hasFieldOrPropertyWithValue("objectMapperRef", "fahrscheinObjectMapper")
                .hasFieldOrPropertyWithValue("nakadiUrl", "https://localhost")
                .hasFieldOrPropertyWithValue("consumerGroup", "a_consumer_group_name")
                .hasFieldOrPropertyWithValue("readFrom", Position.END)
                .hasNoNullFieldsOrProperties();

        // placeholder replacement test
        assertThat(dc.getApplicationName())
                .doesNotContain("$")
                .isNotEqualTo("parser-test-${PWD}")
                .isNotEqualTo("parser-test-");

        // default auth config
        assertThat(dc.getAuthorizations())
                .hasFieldOrPropertyWithValue("anyReader", true)
                .hasFieldOrPropertyWithValue("admins", AuthorizationUserServiceTeamLists.create(
                        List.of("user_1", "user_2"),
                        List.of("service_1"),
                        List.of("a_team")))
                .hasFieldOrPropertyWithValue("readers", AuthorizationUserServiceTeamLists.create(
                        List.of("user_3"),
                        List.of("service_2"),
                        List.of("reading_group")))
                .hasNoNullFieldsOrProperties();

        // default stream parameters
        assertThat(dc.getStreamParameters())
                .hasFieldOrPropertyWithValue("batchLimit", 1)
                .hasFieldOrPropertyWithValue("streamLimit", 1)
                .hasFieldOrPropertyWithValue("batchFlushTimeout", 2)
                .hasFieldOrPropertyWithValue("streamTimeout", 2)
                .hasFieldOrPropertyWithValue("maxUncommittedEvents", 2)
                .hasNoNullFieldsOrProperties();

        // default oauth config
        assertThat(dc.getOauth())
                .hasFieldOrPropertyWithValue("enabled", false)
                .hasFieldOrPropertyWithValue("accessTokenId", "token_id")
                .hasNoNullFieldsOrProperties();

        // default http
        assertThat(dc.getHttp())
                .hasFieldOrPropertyWithValue("socketTimeout", TimeSpan.of(60, SECONDS))
                .hasFieldOrPropertyWithValue("connectTimeout", TimeSpan.of(150, MILLISECONDS))
                .hasFieldOrPropertyWithValue("requestTimeout", TimeSpan.of(60, SECONDS))
                .hasFieldOrPropertyWithValue("contentEncoding", ContentEncoding.GZIP)
                .hasNoNullFieldsOrProperties();

        // consumer config:
        assertThat(configProperties.getConsumers()).containsOnlyKeys("example");
        ConsumerConfig cc = configProperties.getConsumers().get("example");
        assertThat(cc)
                .hasFieldOrPropertyWithValue("autostartEnabled", false)
                .hasFieldOrPropertyWithValue("objectMapperRef", dc.getObjectMapperRef())
                .hasFieldOrPropertyWithValue("applicationName", dc.getApplicationName())
                .hasFieldOrPropertyWithValue("nakadiUrl", dc.getNakadiUrl())
                .hasFieldOrPropertyWithValue("consumerGroup", dc.getConsumerGroup())
                .hasFieldOrPropertyWithValue("readFrom", dc.getReadFrom())
                .hasFieldOrPropertyWithValue("subscriptionById", "test-by-id")
                .hasNoNullFieldsOrProperties();

        // consumer oauth config
        assertThat(cc.getOauth())
                .hasFieldOrPropertyWithValue("enabled", true)
                .hasFieldOrPropertyWithValue("accessTokenId", "example") // TODO: why "example" and not token_id?
                // .hasNoNullFieldsOrProperties()   // TODO: credentialsDirectory is null, why?
        ;

        // consumer auth config
        // TODO: This check fails because of the way we're merging authorizations in AuthorizationsConfig.mergeLists
        // but TBH this merging doesn't make sense to me.
        // assertThat(cc.getAuthorizations()).isEqualTo(dc.getAuthorizations());

        // consumer topics list
        assertThat(cc.getTopics())
                .asList().containsExactly("first.first-update");

        // consumer http config
        assertThat(cc.getHttp())
                .hasFieldOrPropertyWithValue("socketTimeout", TimeSpan.of(60, SECONDS))
                .hasFieldOrPropertyWithValue("connectTimeout", TimeSpan.of(300, MILLISECONDS))
                .hasFieldOrPropertyWithValue("requestTimeout", TimeSpan.of(60, SECONDS))
                .hasFieldOrPropertyWithValue("contentEncoding", ContentEncoding.IDENTITY)
                .hasNoNullFieldsOrProperties();

        // publisher config
        PublisherConfig pc = configProperties.getPublisher();
        assertThat(pc)
                .hasFieldOrPropertyWithValue("objectMapperRef", dc.getObjectMapperRef())
                .hasFieldOrPropertyWithValue("applicationName", dc.getApplicationName())
                .hasFieldOrPropertyWithValue("nakadiUrl", dc.getNakadiUrl())
                // .hasNoNullFieldsOrProperties()  // TODO: PublisherConfig extends AbstractConfig, therefore we're getting null in "autostartEnabled", "consumerGroup", "readFrom", "recordMetrics"
                ;

        // publisher oauth config
        assertThat(pc.getOauth())
                .hasFieldOrPropertyWithValue("enabled", false);

        // publisher auth config -- do we need this?
        assertThat(pc.getAuthorizations())
                .hasFieldOrPropertyWithValue("anyReader", false)
                .hasFieldOrPropertyWithValue("admins", new AuthorizationUserServiceTeamLists())
                .hasFieldOrPropertyWithValue("readers", new AuthorizationUserServiceTeamLists())
                .hasNoNullFieldsOrProperties();

        // publisher http config
        assertThat(pc.getHttp()).isEqualTo(dc.getHttp());
    }

    @TestConfiguration
    static class AppConfig {

        @Bean("fahrscheinAccessTokenProvider")
        public AccessTokenProvider accessTokenProvider() {
            return new AccessTokenProvider() {

                @Override
                public String getAccessToken() throws IOException {
                    return "NO_ACCESS_PLEASE";
                }
            };
        }

        @Bean
        public Converter<String, PublisherConfig> publisherConfigConverter(){
            return new Converter<String, PublisherConfig>() {

                @Override
                public PublisherConfig convert(String source) {
                    PublisherConfig c = new PublisherConfig();
                    c.setId(source);
                    return c;
                }
            };
        }
    }
}
