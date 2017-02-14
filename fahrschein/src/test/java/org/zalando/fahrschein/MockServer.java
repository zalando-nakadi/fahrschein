package org.zalando.fahrschein;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

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

class MockServer implements ClientHttpRequestFactory {

    private static final Configuration JSON_CONFIGURATION = Configuration.builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonProvider())
            .build();

    private URI expectedUri;
    private HttpMethod expectedMethod;
    private MediaType expectedContentType;

    private LinkedHashMap<String, Matcher<Object>> expectedJsonPaths;
    private LinkedHashMap<String, Matcher<String>> expectedHeaders;

    private HttpHeaders requestHeaders;
    private ByteArrayOutputStream requestBody;

    @Nullable
    private MediaType responseContentType;
    private HttpStatus responseStatus;
    private String responseBody;

    private ClientHttpRequestFactory clientHttpRequestFactory;
    private ClientHttpRequest clientHttpRequest;

    public MockServer() {
        this.expectedJsonPaths = new LinkedHashMap<>();
        this.expectedHeaders = new LinkedHashMap<>();
    }

    public MockServer expectRequestTo(URI expectedUri, HttpMethod expectedMethod) {
        this.expectedUri = expectedUri;
        this.expectedMethod = expectedMethod;
        return this;
    }

    public MockServer expectRequestTo(String expectedUri, HttpMethod expectedMethod) {
        return expectRequestTo(URI.create(expectedUri), expectedMethod);
    }

    public MockServer andExpectContentType(MediaType expectedContentType) {
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

    public MockServer andRespondWith(HttpStatus responseStatus, @Nullable MediaType responseContentType, String responseBody) {
        this.responseStatus = responseStatus;
        this.responseContentType = responseContentType;
        this.responseBody = responseBody;
        return this;
    }

    public MockServer andRespondWith(HttpStatus responseStatus) {
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
        clientHttpRequest = mock(ClientHttpRequest.class);

        final HttpHeaders responseHeaders = new HttpHeaders();
        if (responseContentType != null) {
            responseHeaders.setContentType(responseContentType);
        }

        final ClientHttpResponse clientHttpResponse = mock(ClientHttpResponse.class);
        when(clientHttpResponse.getStatusCode()).thenReturn(responseStatus);
        when(clientHttpResponse.getRawStatusCode()).thenReturn(responseStatus.value());
        when(clientHttpResponse.getStatusText()).thenReturn(responseStatus.getReasonPhrase());
        when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
        when(clientHttpResponse.getHeaders()).thenReturn(responseHeaders);

        when(clientHttpRequest.execute()).thenReturn(clientHttpResponse);
        when(clientHttpRequest.getURI()).thenReturn(expectedUri);
        when(clientHttpRequest.getMethod()).thenReturn(expectedMethod);
        when(clientHttpRequest.getBody()).thenReturn(requestBody = new ByteArrayOutputStream());
        when(clientHttpRequest.getHeaders()).thenReturn(requestHeaders = new HttpHeaders());

        clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);
        when(clientHttpRequestFactory.createRequest(expectedUri, expectedMethod)).thenReturn(clientHttpRequest);
    }

    public void verify() throws IOException {
        Mockito.verify(clientHttpRequest).execute();
        Mockito.verify(clientHttpRequestFactory).createRequest(expectedUri, expectedMethod);

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
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return clientHttpRequestFactory.createRequest(uri, httpMethod);
    }
}
