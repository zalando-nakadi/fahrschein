package org.zalando.fahrschein;

import org.junit.jupiter.api.Test;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.HeadersImpl;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.Response;
import org.zalando.fahrschein.http.api.UserAgent;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserAgentRequestFactoryTest {
    static class DummyRequestFactory implements RequestFactory {

        @Override
        public Request createRequest(URI uri, String method) throws IOException {
            return new Request() {
                private final Headers headers = new HeadersImpl();
                @Override
                public String getMethod() {
                    return method;
                }

                @Override
                public URI getURI() {
                    return uri;
                }

                @Override
                public Headers getHeaders() {
                    return headers;
                }

                @Override
                public OutputStream getBody() throws IOException {
                    return null;
                }

                @Override
                public Response execute() throws IOException {
                    return null;
                }
            };
        }
    }

    @Test
    public void appendUserAgentToRequest() throws IOException {
        UserAgent ua = new UserAgent(DummyRequestFactory.class);
        UserAgentRequestFactory rf = new UserAgentRequestFactory(new DummyRequestFactory());
        Request r = rf.createRequest(URI.create("dummy://req"), "POST");
        assertEquals(ua.userAgent(), r.getHeaders().getFirst("User-Agent"));
    }
}
