package org.zalando.fahrschein.http.apache;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.test.AbstractRequestFactoryTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.SocketTimeoutException;

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
            }
        });

        // when
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1).build();
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
