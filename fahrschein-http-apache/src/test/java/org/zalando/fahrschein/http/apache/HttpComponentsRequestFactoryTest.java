package org.zalando.fahrschein.http.apache;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.test.AbstractRequestFactoryTest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
public class HttpComponentsRequestFactoryTest extends AbstractRequestFactoryTest {

    @Test
    public void testTimeout() throws IOException {
        // given
        server.createContext("/timeout", exchange -> {
            try {
                Thread.sleep(10l);
                exchange.sendResponseHeaders(201, 0);
            } catch (InterruptedException e) {
                fail("Unexpected Exception thrown");
            }
        });

        // when
        RequestConfig requestConfig = RequestConfig.custom().setResponseTimeout(1, TimeUnit.MILLISECONDS).build();
        final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        RequestFactory f = new HttpComponentsRequestFactory(httpClient, ContentEncoding.GZIP);
        Request r = f.createRequest(serverAddress.resolve("/timeout"), "GET");

        assertThrows(SocketTimeoutException.class, () -> {
            r.execute();
        });
    }

    public RequestFactory defaultRequestFactory(ContentEncoding contentEncoding) {
        return new HttpComponentsRequestFactory(HttpClients.createDefault(), contentEncoding);
    }

}
