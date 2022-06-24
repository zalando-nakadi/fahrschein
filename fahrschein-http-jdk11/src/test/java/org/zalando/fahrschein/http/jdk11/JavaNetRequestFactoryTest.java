package org.zalando.fahrschein.http.jdk11;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.test.AbstractRequestFactoryTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class JavaNetRequestFactoryTest extends AbstractRequestFactoryTest {

    @Override
    public RequestFactory defaultRequestFactory(ContentEncoding contentEncoding) {
        return new JavaNetRequestFactory(HttpClient.newHttpClient(), Optional.empty(), contentEncoding);
    }

    @Test
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
        assertThrows(HttpTimeoutException.class, () -> r.execute());
    }

}
