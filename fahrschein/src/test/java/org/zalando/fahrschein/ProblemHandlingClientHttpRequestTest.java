package org.zalando.fahrschein;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.when;

public class ProblemHandlingClientHttpRequestTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final ClientHttpRequest clientHttpRequest = Mockito.mock(ClientHttpRequest.class);
    private final ClientHttpResponse clientHttpResponse = Mockito.mock(ClientHttpResponse.class);
    private final ProblemHandlingClientHttpRequest problemHandlingClientHttpRequest = new ProblemHandlingClientHttpRequest(clientHttpRequest);

    @Test
    public void shouldCreateProblemFromStatusAndText() throws IOException {
        when(clientHttpResponse.getRawStatusCode()).thenReturn(HttpStatus.CONFLICT.value());
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
        when(clientHttpResponse.getStatusText()).thenReturn("conflict");

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        when(clientHttpResponse.getHeaders()).thenReturn(headers);

        when(clientHttpRequest.execute()).thenReturn(clientHttpResponse);

        expectedException.expect(instanceOf(IOProblem.class));
        expectedException.expect(hasProperty("statusCode", equalTo(HttpStatus.CONFLICT.value())));
        expectedException.expect(hasProperty("type", equalTo(URI.create("about:blank"))));
        expectedException.expect(hasProperty("title", equalTo("conflict")));
        expectedException.expect(hasProperty("detail", equalTo(null)));

        problemHandlingClientHttpRequest.execute();
    }

    @Test
    public void shouldDeserializeProblemJson() throws IOException {
        when(clientHttpResponse.getRawStatusCode()).thenReturn(HttpStatus.NOT_FOUND.value());
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(clientHttpResponse.getStatusText()).thenReturn("not found");
        when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream("{\"type\":\"http://httpstatus.es/404\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"EventType does not exist.\"}".getBytes(StandardCharsets.UTF_8)));

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/problem+json"));
        when(clientHttpResponse.getHeaders()).thenReturn(headers);

        when(clientHttpRequest.execute()).thenReturn(clientHttpResponse);

        expectedException.expect(instanceOf(IOProblem.class));
        expectedException.expect(hasProperty("statusCode", equalTo(HttpStatus.NOT_FOUND.value())));
        expectedException.expect(hasProperty("type", equalTo(URI.create("http://httpstatus.es/404"))));
        expectedException.expect(hasProperty("title", equalTo("Not Found")));
        expectedException.expect(hasProperty("detail", equalTo("EventType does not exist.")));

        problemHandlingClientHttpRequest.execute();
    }

    @Test
    public void shouldDeserializeOAuthError() throws IOException {
        when(clientHttpResponse.getRawStatusCode()).thenReturn(HttpStatus.BAD_REQUEST.value());
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(clientHttpResponse.getStatusText()).thenReturn("bad request");
        when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream("{\"error\":\"invalid_request\",\"error_description\":\"Access Token not valid\"}".getBytes(StandardCharsets.UTF_8)));

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/json"));
        when(clientHttpResponse.getHeaders()).thenReturn(headers);

        when(clientHttpRequest.execute()).thenReturn(clientHttpResponse);

        expectedException.expect(instanceOf(IOProblem.class));
        expectedException.expect(hasProperty("statusCode", equalTo(HttpStatus.BAD_REQUEST.value())));
        expectedException.expect(hasProperty("type", equalTo(URI.create("about:blank"))));
        expectedException.expect(hasProperty("title", equalTo("invalid_request")));
        expectedException.expect(hasProperty("detail", equalTo("Access Token not valid")));

        problemHandlingClientHttpRequest.execute();
    }

}
