package org.zalando.fahrschein.http.apache;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.test.AbstractRequestFactoryTest;

import java.io.IOException;
import java.net.SocketTimeoutException;

@RunWith(MockitoJUnitRunner.class)
public class HttpComponentsRequestFactoryTest extends AbstractRequestFactoryTest {

    public RequestFactory defaultRequestFactory(ContentEncoding contentEncoding) {
        return new HttpComponentsRequestFactory(HttpClients.createDefault(), contentEncoding);
    }

    @Test(expected = SocketTimeoutException.class)
    public void testTimeout() throws IOException {
        // given
        server.createContext("/timeout", exchange -> {
            try {
                Thread.sleep(10l);
                exchange.sendResponseHeaders(201, 0);
            } catch (InterruptedException e) { }
        });

        // when
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1).build();
        final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        RequestFactory f = new HttpComponentsRequestFactory(httpClient, ContentEncoding.GZIP);
        Request r = f.createRequest(serverAddress.resolve("/timeout"), "GET");
        r.execute();
    }

}
