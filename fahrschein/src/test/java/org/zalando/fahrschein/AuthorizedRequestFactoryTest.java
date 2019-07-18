package org.zalando.fahrschein;

import org.junit.Before;
import org.junit.Test;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.HeadersImpl;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import java.io.IOException;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthorizedRequestFactoryTest {

    private static final String BEARER_TOKEN = "Bearer token";
    private final RequestFactory delegate = mock(RequestFactory.class);
    private final AuthorizedRequestFactory unit = new AuthorizedRequestFactory(delegate, () -> BEARER_TOKEN);

    @Before
    public void setUp() throws Exception {
        final Request request = mockRequest();
        when(delegate.createRequest(any(), anyString())).thenReturn(request);
    }

    @Test
    public void shouldDelegate() throws IOException {
        final URI uri = URI.create("localhost");
        final String method = "GET";

        unit.createRequest(uri, method);
        verify(delegate).createRequest(uri, method);
    }

    @Test
    public void shouldAddAuthorizationHeader() throws IOException {
        final Request request = unit.createRequest(URI.create("localhost"), "POST");
        assertThat(request.getHeaders().headerNames(), contains(Headers.AUTHORIZATION));
        assertThat(request.getHeaders().getFirst(Headers.AUTHORIZATION), equalTo(BEARER_TOKEN));
    }


    private static Request mockRequest() {
        final Request request = mock(Request.class);
        when(request.getHeaders()).thenReturn(new HeadersImpl());
        return request;
    }
}
