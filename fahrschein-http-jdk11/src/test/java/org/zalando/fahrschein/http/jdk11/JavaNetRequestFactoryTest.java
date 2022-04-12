package org.zalando.fahrschein.http.jdk11;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.test.AbstractRequestFactoryTest;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class JavaNetRequestFactoryTest extends AbstractRequestFactoryTest {

    @Override
    public RequestFactory defaultRequestFactory(ContentEncoding contentEncoding) {
        return new JavaNetRequestFactory(HttpClient.newHttpClient(), Optional.empty(), contentEncoding);
    }

    @Test(expected = java.net.http.HttpTimeoutException.class)
    public void testTimeout() throws IOException {
        // given
        server.createContext("/timeout", exchange -> {
            try {
                Thread.sleep(10l);
                exchange.sendResponseHeaders(201, 0);
            } catch (InterruptedException e) { }
        });

        // when
        RequestFactory f = new JavaNetRequestFactory(HttpClient.newBuilder().build(), Optional.of(Duration.ofMillis(1)), ContentEncoding.IDENTITY);
        Request r = f.createRequest(serverAddress.resolve("/timeout"), "GET");
        r.execute();
    }

}
