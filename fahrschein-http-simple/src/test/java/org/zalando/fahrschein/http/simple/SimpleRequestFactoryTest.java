package org.zalando.fahrschein.http.simple;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.test.AbstractRequestFactoryTest;

import java.io.IOException;
import java.net.SocketTimeoutException;

@RunWith(MockitoJUnitRunner.class)
public class SimpleRequestFactoryTest extends AbstractRequestFactoryTest {

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
        SimpleRequestFactory f = new SimpleRequestFactory(ContentEncoding.IDENTITY);
        f.setReadTimeout(1);
        Request r = f.createRequest(serverAddress.resolve("/timeout"), "GET");
        r.execute();
    }

    @Override
    public RequestFactory defaultRequestFactory(ContentEncoding contentEncoding) {
        return new SimpleRequestFactory(contentEncoding);
    }
}
