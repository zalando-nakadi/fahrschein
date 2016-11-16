package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class NakadiClientTest {

    private MockRestServiceServer server;
    private NakadiClient client;

    @Before
    public void foo() {
        final RestTemplate restTemplate = new RestTemplate();
        final MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        final ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();

        final NakadiClient nakadiClient = new NakadiClient(URI.create("http://example.com/"), requestFactory, new NoBackoffStrategy(), new ObjectMapper(), new InMemoryCursorManager());

        this.server = mockServer;
        this.client = nakadiClient;
    }

    @Test
    public void shouldGetPartitions() throws IOException {
        server.expect(requestTo("http://example.com/event-types/foobar/partitions"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"partition\":\"0\", \"oldest_available_offset\":\"10\", \"newest_available_offset\":\"20\"},{\"partition\":\"1\", \"oldest_available_offset\":\"BEGIN\",\"newest_available_offset\":\"10\"}]", MediaType.APPLICATION_JSON));

        final List<Partition> partitions = client.getPartitions("foobar");

        assertNotNull(partitions);
        assertThat(partitions, hasSize(2));
    }

    @Test
    public void shouldPostSubscription() throws IOException {
        server.expect(requestTo("http://example.com/subscriptions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.owning_application", equalTo("nakadi-client-test")))
                .andExpect(jsonPath("$.event_types[0]", equalTo("foo")))
                .andExpect(jsonPath("$.consumer_group", equalTo("bar")))
                .andRespond(withSuccess("{\"id\":\"1234\",\"owning_application\":\"nakadi-client-test\",\"event_types\":[\"foo\"],\"consumer_group\":\"bar\",\"created_at\":\"2016-11-15T15:23:42.123+01:00\"}", MediaType.APPLICATION_JSON));

        final Subscription subscription = client.subscribe("nakadi-client-test", "foo", "bar");

        assertNotNull(subscription);
        assertEquals("1234", subscription.getId());
        assertEquals("nakadi-client-test", subscription.getOwningApplication());
        assertEquals(Collections.singleton("foo"), subscription.getEventTypes());
        assertEquals("bar", subscription.getConsumerGroup());
        assertNotNull(subscription.getCreatedAt());
    }

}
