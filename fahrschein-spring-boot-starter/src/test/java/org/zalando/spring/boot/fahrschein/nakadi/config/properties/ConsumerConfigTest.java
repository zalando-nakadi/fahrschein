package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.junit.jupiter.api.Test;
import org.zalando.fahrschein.http.api.ContentEncoding;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsumerConfigTest {

    private static final int STREAM_TIMEOUT = 5;
    private static final int STREAM_LIMIT = 23;
    private static final int STREAM_KEEP_ALIVE_LIMIT = 34;
    private static final int MAX_UNCOMMITTED_EVENTS = 9000;
    private static final int BATCH_LIMIT = 1000;
    private static final int BATCH_FLUSH_TIMEOUT = 12;
    private static final String USER_4 = "user_4";
    private static final String USER_5 = "user_5";
    private static final String TEST_TEAM = "team";
    private static final String CONSUMER_GROUP = "TestConsumerGroup";
    private static final String ACCESS_TOKEN_ID = "testAccessTokenId";
    private static final String NAKADI_URI = "http://localhost";
    private static final String APPLICATION_NAME = "TestApp";

    private static final String SUBSCRIPTION_ID = "test-id";

    @Test
    public void testInitialConsumerConfig() {
        ConsumerConfig cc = new ConsumerConfig();
        assertThat(cc.getAutostartEnabled()).isNull();
        assertThat(cc.getApplicationName()).isNull();
        assertThat(cc.getConsumerGroup()).isNull();
        assertThat(cc.getId()).isNull();
        assertThat(cc.getNakadiUrl()).isNull();
        assertThat(cc.getReadFrom()).isNull();


        assertThat(cc.getTopics()).isNotNull();
        assertThat(cc.getTopics()).isEmpty();

        assertThat(cc.getStreamParameters()).isNotNull();
        assertInitialStreamPrarametersAreNull(cc.getStreamParameters());

        assertThat(cc.getOauth()).isNotNull();
        assertInitialOauth(cc.getOauth());

        assertThat(cc.getAuthorizations()).isNotNull();
        assertInitialAuthorizations(cc.getAuthorizations());

        assertThat(cc.getHttp()).isNotNull();
        assertInitialSubscriptionConfig(cc.getSubscription());
    }

    private void assertInitialAuthorizations(AuthorizationsConfig authorizations) {
        assertThat(authorizations.getAdmins().getServices()).isEmpty();
        assertThat(authorizations.getAdmins().getUsers()).isEmpty();
        assertThat(authorizations.getAdmins().getTeams()).isEmpty();
        assertThat(authorizations.getReaders().getServices()).isEmpty();
        assertThat(authorizations.getReaders().getUsers()).isEmpty();
        assertThat(authorizations.getReaders().getTeams()).isEmpty();
        assertThat(authorizations.getAnyReader()).isFalse();
    }

    private void assertInitialOauth(OAuthConfig oauth) {
        assertThat(oauth.getAccessTokenId()).isNull();
        assertThat(oauth.getEnabled()).isEqualTo(Boolean.FALSE);
    }

    private void assertInitialSubscriptionConfig(SubscriptionConfig subscriptions) {
        assertThat(subscriptions.getEnabled()).isFalse();
        assertThat(subscriptions.getSubscriptionId()).isNull();
    }

    private void assertInitialStreamPrarametersAreNull(StreamParametersConfig streamParameterConfig) {
        assertThat(streamParameterConfig.getBatchFlushTimeout()).isNull();
        assertThat(streamParameterConfig.getBatchLimit()).isNull();
        assertThat(streamParameterConfig.getMaxUncommittedEvents()).isNull();
        assertThat(streamParameterConfig.getStreamKeepAliveLimit()).isNull();
        assertThat(streamParameterConfig.getStreamLimit()).isNull();
        assertThat(streamParameterConfig.getStreamTimeout()).isNull();
    }

    @Test
    public void testMergedConsumerConfig() {
        DefaultConsumerConfig dc = getDefaultConsumerConfig();
        dc.setObjectMapperRef("jacksonObjectMapper");
        ConsumerConfig cc = new ConsumerConfig();

        cc.getAuthorizations().setAdmins(AuthorizationUserServiceTeamLists.create(List.of(USER_5), List.of(), List.of(TEST_TEAM)));

        cc.mergeWithDefaultConfig(dc);

        assertThat(cc.getObjectMapperRef()).isEqualTo(dc.getObjectMapperRef());

        assertThat(cc.getId()).isNotEqualTo(dc.getId());

        assertThat(cc.getApplicationName()).isEqualTo(APPLICATION_NAME);
        assertThat(cc.getNakadiUrl()).isEqualTo(NAKADI_URI);
        assertThat(cc.getConsumerGroup()).isEqualTo(CONSUMER_GROUP);

        assertThat(cc.getOauth().getAccessTokenId()).isEqualTo(ACCESS_TOKEN_ID);
        assertThat(cc.getOauth().getEnabled()).isTrue();

        assertThat(cc.getAuthorizations().getAdmins().getUsers()).contains(USER_4, USER_5);
        assertThat(cc.getAuthorizations().getAdmins().getTeams()).contains(TEST_TEAM);
        assertThat(cc.getAuthorizations().getAnyReader()).isEqualTo(Boolean.TRUE);

        assertThat(cc.getStreamParameters().getBatchFlushTimeout()).isEqualTo(BATCH_FLUSH_TIMEOUT);
        assertThat(cc.getStreamParameters().getBatchLimit()).isEqualTo(BATCH_LIMIT);
        assertThat(cc.getStreamParameters().getMaxUncommittedEvents()).isEqualTo(MAX_UNCOMMITTED_EVENTS);
        assertThat(cc.getStreamParameters().getStreamKeepAliveLimit()).isEqualTo(STREAM_KEEP_ALIVE_LIMIT);
        assertThat(cc.getStreamParameters().getStreamLimit()).isEqualTo(STREAM_LIMIT);
        assertThat(cc.getStreamParameters().getStreamTimeout()).isEqualTo(STREAM_TIMEOUT);

        assertThat(cc.getHttp().getContentEncoding()).isEqualTo(ContentEncoding.GZIP);

        assertThat(cc.getSubscription().getEnabled()).isTrue();
        assertThat(cc.getSubscription().getSubscriptionId()).isEqualTo(SUBSCRIPTION_ID);
    }

    private DefaultConsumerConfig getDefaultConsumerConfig() {
        DefaultConsumerConfig dc = new DefaultConsumerConfig();
        dc.setApplicationName(APPLICATION_NAME);
        AuthorizationsConfig a = new AuthorizationsConfig();
        a.getAdmins().getUsers().add(USER_4);
        a.setAnyReader(Boolean.TRUE);

        dc.setAuthorizations(a);

        dc.setAutostartEnabled(Boolean.FALSE);
        dc.setConsumerGroup(CONSUMER_GROUP);


        dc.setId("testId");

        dc.setNakadiUrl(NAKADI_URI);
        dc.getOauth().setAccessTokenId(ACCESS_TOKEN_ID);
        dc.getOauth().setEnabled(Boolean.TRUE);

        dc.getStreamParameters().setBatchFlushTimeout(BATCH_FLUSH_TIMEOUT);
        dc.getStreamParameters().setBatchLimit(BATCH_LIMIT);
        dc.getStreamParameters().setMaxUncommittedEvents(MAX_UNCOMMITTED_EVENTS);
        dc.getStreamParameters().setStreamKeepAliveLimit(STREAM_KEEP_ALIVE_LIMIT);
        dc.getStreamParameters().setStreamLimit(STREAM_LIMIT);
        dc.getStreamParameters().setStreamTimeout(STREAM_TIMEOUT);

        dc.getSubscription().setEnabled(Boolean.TRUE);
        dc.getSubscription().setSubscriptionId(SUBSCRIPTION_ID);

        return dc;
    }

}
