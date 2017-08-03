package org.zalando.fahrschein;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.HeadersImpl;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.Response;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MockServer implements RequestFactory {

    private static final Configuration JSON_CONFIGURATION = Configuration.builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonProvider())
            .build();

    private URI expectedUri;
    private String expectedMethod;
    private ContentType expectedContentType;

    private LinkedHashMap<String, Matcher<Object>> expectedJsonPaths;
    private LinkedHashMap<String, Matcher<String>> expectedHeaders;

    private Headers requestHeaders;
    private ByteArrayOutputStream requestBody;

    @Nullable
    private ContentType responseContentType;
    private int responseStatus;
    private String responseBody;

    private RequestFactory requestFactory;
    private Request request;

    public MockServer() {
        this.expectedJsonPaths = new LinkedHashMap<>();
        this.expectedHeaders = new LinkedHashMap<>();
    }

    public MockServer expectRequestTo(URI expectedUri, String expectedMethod) {
        this.expectedUri = expectedUri;
        this.expectedMethod = expectedMethod;
        return this;
    }

    public MockServer expectRequestTo(String expectedUri, String expectedMethod) {
        return expectRequestTo(URI.create(expectedUri), expectedMethod);
    }

    public MockServer andExpectContentType(ContentType expectedContentType) {
        this.expectedContentType = expectedContentType;
        return this;
    }

    public MockServer andExpectHeader(String key, Matcher<String> matcher) {
        this.expectedHeaders.put(key, matcher);
        return this;
    }

    public MockServer andExpectHeader(String key, String value) {
        return andExpectHeader(key, Matchers.equalTo(value));
    }

    public MockServer andRespondWith(int responseStatus, @Nullable ContentType responseContentType, String responseBody) {
        this.responseStatus = responseStatus;
        this.responseContentType = responseContentType;
        this.responseBody = responseBody;
        return this;
    }

    public MockServer andRespondWith(int responseStatus) {
        return andRespondWith(responseStatus, null, "");
    }

    @SuppressWarnings("unchecked")
    public <T> MockServer andExpectJsonPath(String jsonPath, Matcher<T> matcher) {
        expectedJsonPaths.put(jsonPath, (Matcher<Object>)matcher);
        return this;
    }

    public MockServer andExpectJsonPath(String jsonPath, Object value) {
        return andExpectJsonPath(jsonPath, Matchers.equalTo(value));
    }

    public void setup() throws IOException {
        request = mock(Request.class);

        final Headers responseHeaders = new HeadersImpl();
        if (responseContentType != null) {
            responseHeaders.setContentType(responseContentType);
        }

        final Response clientHttpResponse = mock(Response.class);
        when(clientHttpResponse.getStatusCode()).thenReturn(responseStatus);
        when(clientHttpResponse.getStatusCode()).thenReturn(responseStatus);
        when(clientHttpResponse.getStatusText()).thenReturn("foobar");
        when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
        when(clientHttpResponse.getHeaders()).thenReturn(responseHeaders);

        when(request.execute()).thenReturn(clientHttpResponse);
        when(request.getURI()).thenReturn(expectedUri);
        when(request.getMethod()).thenReturn(expectedMethod);
        when(request.getBody()).thenReturn(requestBody = new ByteArrayOutputStream());
        when(request.getHeaders()).thenReturn(requestHeaders = new HeadersImpl());

        requestFactory = mock(RequestFactory.class);
        when(requestFactory.createRequest(expectedUri, expectedMethod)).thenReturn(request);
    }

    public void verify() throws IOException {
        Mockito.verify(request).execute();
        Mockito.verify(requestFactory).createRequest(expectedUri, expectedMethod);

        if (expectedContentType != null) {
            assertEquals("requestContentType", requestHeaders.getContentType(), expectedContentType);
        }

        for (Map.Entry<String, Matcher<String>> entry : expectedHeaders.entrySet()) {
            final String key = entry.getKey();
            final Matcher<String> matcher = entry.getValue();
            assertThat(key, requestHeaders.getFirst(key), matcher);
        }


        if (!expectedJsonPaths.isEmpty()) {
            final String body = new String(requestBody.toByteArray(), StandardCharsets.UTF_8);
            for (Map.Entry<String, Matcher<Object>> entry : expectedJsonPaths.entrySet()) {
                final String path = entry.getKey();
                final JsonPath jsonPath = JsonPath.compile(path);
                final Object value = jsonPath.read(body, JSON_CONFIGURATION);
                final Matcher<Object> matcher = entry.getValue();
                assertThat(path, value, matcher);
            }
        }
    }

    @Override
    public Request createRequest(URI uri, String method) throws IOException {
        return requestFactory.createRequest(uri, method);
    }
}
