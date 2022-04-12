package org.zalando.fahrschein;

import org.junit.Test;
import org.mockito.Mockito;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.HeadersImpl;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class IdentityAcceptEncodingRequestFactoryTest {

    private final Request request = Mockito.mock(Request.class);
    private final RequestFactory delegate = Mockito.mock(RequestFactory.class);

    @Test
    public void shouldSetAcceptEncodingHeader() throws IOException {
        final Headers headers = new HeadersImpl();
        when(request.getHeaders()).thenReturn(headers);
        when(delegate.createRequest(any(URI.class), any(String.class))).thenReturn(request);

        IdentityAcceptEncodingRequestFactory SUT = new IdentityAcceptEncodingRequestFactory(delegate);
        SUT.createRequest(URI.create("any://uri"), "GET");

        assertEquals(Arrays.asList("identity"), headers.get("Accept-Encoding"));
    }

    @Test
    public void shouldOverrideExistingAcceptEncodingHeader() throws IOException {
        final Headers headers = new HeadersImpl();
        headers.put("Accept-Encoding", "gzip");
        when(request.getHeaders()).thenReturn(headers);
        when(delegate.createRequest(any(URI.class), any(String.class))).thenReturn(request);

        IdentityAcceptEncodingRequestFactory SUT = new IdentityAcceptEncodingRequestFactory(delegate);
        SUT.createRequest(URI.create("any://uri"), "GET");

        assertEquals(Arrays.asList("identity"), headers.get("Accept-Encoding"));
    }

}
