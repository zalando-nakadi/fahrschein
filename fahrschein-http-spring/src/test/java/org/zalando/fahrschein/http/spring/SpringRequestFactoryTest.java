package org.zalando.fahrschein.http.spring;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.test.AbstractRequestFactoryTest;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class SpringRequestFactoryTest extends AbstractRequestFactoryTest {

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
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(1, TimeUnit.MILLISECONDS)
                .build();
        OkHttp3ClientHttpRequestFactory clientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory(client);
        SpringRequestFactory f = new SpringRequestFactory(clientHttpRequestFactory, ContentEncoding.IDENTITY);
        Request r = f.createRequest(serverAddress.resolve("/timeout"), "GET");
        assertThrows(SocketTimeoutException.class, () -> r.execute());
    }

    @Override
    public RequestFactory defaultRequestFactory(ContentEncoding contentEncoding) {
        final OkHttpClient client = new OkHttpClient.Builder()
                .build();
        final OkHttp3ClientHttpRequestFactory clientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory(client);
        final SpringRequestFactory f = new SpringRequestFactory(clientHttpRequestFactory, contentEncoding);
        return f;
    }
}
