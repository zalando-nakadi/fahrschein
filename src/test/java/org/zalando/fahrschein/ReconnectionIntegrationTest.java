package org.zalando.fahrschein;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.zalando.fahrschein.request.CloseableHttpComponentsClientHttpRequestFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class ReconnectionIntegrationTest {

    static class StreamingHandler extends AbstractHandler {

        private static final String SOME_EVENT = "{\"cursor\":{\"partition\":\"%s\",\"offset\":\"%s\"},\"events\":[{\"id\":\"%s\"}]}\n";

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/x-json-stream");

            try (PrintWriter writer = response.getWriter()) {
                for (int i = 0; i < 10; i++) {
                    writer.printf(SOME_EVENT, "0", String.valueOf(i), String.valueOf(i));
                    writer.flush();
                    try {
                        Thread.sleep(1*1000L);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } finally {
                // Inform jetty that this request has now been handled
                baseRequest.setHandled(true);
            }
        }
    }

    public static class SomeEvent {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    private static Server server;

    @BeforeClass
    public static void startJetty() throws Exception {
        server = new Server(8088);
        server.setHandler(new StreamingHandler());

        server.start();
    }

    @AfterClass
    public static void stopJetty() throws Exception {
        server.stop();
    }

    private NakadiClient nakadiClient;

    @Before
    public void setupNakadiClient() {
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(30000)
                .setConnectionRequestTimeout(2000)
                .build();

        final CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionTimeToLive(2, TimeUnit.SECONDS)
                .disableAutomaticRetries()
                .setDefaultRequestConfig(config)
                .disableRedirectHandling()
                .build();

        final ClientHttpRequestFactory requestFactory = new CloseableHttpComponentsClientHttpRequestFactory(httpClient);
        final BackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final InMemoryCursorManager cursorManager = new InMemoryCursorManager();

        this.nakadiClient = new NakadiClient(URI.create("http://localhost:8088/"), requestFactory, backoffStrategy, objectMapper, cursorManager);
    }

    @Test(timeout = 5000)
    public void shouldNotConsumeStream() throws IOException, BackoffException {
        final Listener<SomeEvent> listener = (events) -> {
            throw new IOException("IOException while processing event");
        };
        nakadiClient.listen("some-event", SomeEvent.class, listener);
    }
}
