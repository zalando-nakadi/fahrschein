package org.zalando.fahrschein;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.HeadersImpl;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class ProblemHandlingRequestTest {

    private final Request request = Mockito.mock(Request.class);
    private final Response response = Mockito.mock(Response.class);
    private final ProblemHandlingRequest problemHandlingRequest = new ProblemHandlingRequest(request);

    @Test
    public void shouldCreateProblemWhenTypeIsMissing() throws IOException {
        when(response.getStatusCode()).thenReturn(422);
        when(response.getStatusText()).thenReturn("Unprocessable Entity");
        when(response.getBody()).thenReturn(new ByteArrayInputStream("{ \"title\":\"Unprocessable Entity\",\"status\":422, \"detail\": \"Session with stream id not found\" }".getBytes(StandardCharsets.UTF_8)));

        final Headers headers = new HeadersImpl();
        headers.setContentType(ContentType.APPLICATION_PROBLEM_JSON);
        when(response.getHeaders()).thenReturn(headers);

        when(request.execute()).thenReturn(response);

        IOProblem expectedException = assertThrows(IOProblem.class, () -> {
            problemHandlingRequest.execute();
        });

        assertThat(expectedException.getStatusCode(), equalTo(422));
        assertThat(expectedException.getType(), equalTo(URI.create("about:blank")));
        assertThat(expectedException.getTitle(), equalTo("Unprocessable Entity"));
        assertThat(expectedException.getDetail(), equalTo(Optional.of("Session with stream id not found")));
    }

    @Test
    public void shouldCreateProblemFromStatusAndText() throws IOException {
        when(response.getStatusCode()).thenReturn(409);
        when(response.getStatusText()).thenReturn("conflict");

        final Headers headers = new HeadersImpl();
        headers.setContentType(ContentType.TEXT_PLAIN);
        when(response.getHeaders()).thenReturn(headers);

        when(request.execute()).thenReturn(response);

        IOProblem expectedException = assertThrows(IOProblem.class, () -> {
            problemHandlingRequest.execute();
        });

        assertThat(expectedException.getStatusCode(), equalTo(409));
        assertThat(expectedException.getType(), equalTo(URI.create("about:blank")));
        assertThat(expectedException.getTitle(), equalTo("conflict"));
        assertThat(expectedException.getDetail(), equalTo(Optional.<String>empty()));
    }

    @Test
    public void shouldDeserializeProblemJson() throws IOException {
        when(response.getStatusCode()).thenReturn(404);
        when(response.getStatusText()).thenReturn("not found");
        when(response.getBody()).thenReturn(new ByteArrayInputStream("{\"type\":\"http://httpstatus.es/404\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"EventType does not exist.\"}".getBytes(StandardCharsets.UTF_8)));

        final Headers headers = new HeadersImpl();
        headers.setContentType(ContentType.APPLICATION_PROBLEM_JSON);
        when(response.getHeaders()).thenReturn(headers);

        when(request.execute()).thenReturn(response);

        IOProblem expectedException = assertThrows(IOProblem.class, () -> {
            problemHandlingRequest.execute();
        });

        assertThat(expectedException.getStatusCode(), equalTo(404));
        assertThat(expectedException.getType(), equalTo(URI.create("http://httpstatus.es/404")));
        assertThat(expectedException.getTitle(), equalTo("Not Found"));
        assertThat(expectedException.getDetail(), equalTo(Optional.of("EventType does not exist.")));

    }

    @Test
    public void shouldDeserializeOAuthError() throws IOException {
        when(response.getStatusCode()).thenReturn(400);
        when(response.getStatusText()).thenReturn("bad request");
        when(response.getBody()).thenReturn(new ByteArrayInputStream("{\"error\":\"invalid_request\",\"error_description\":\"Access Token not valid\"}".getBytes(StandardCharsets.UTF_8)));

        final Headers headers = new HeadersImpl();
        headers.setContentType(ContentType.APPLICATION_JSON);
        when(response.getHeaders()).thenReturn(headers);

        when(request.execute()).thenReturn(response);

        IOProblem expectedException = assertThrows(IOProblem.class, () -> {
            problemHandlingRequest.execute();
        });
        assertThat(expectedException.getStatusCode(), equalTo(400));
        assertThat(expectedException.getType(), equalTo(URI.create("about:blank")));
        assertThat(expectedException.getTitle(), equalTo("invalid_request"));
        assertThat(expectedException.getDetail(), equalTo(Optional.of("Access Token not valid")));
    }


    @Test
    public void shouldHandleMultiStatus() throws IOException {
        when(response.getStatusCode()).thenReturn(207);
        when(response.getStatusText()).thenReturn("Multistatus");
        when(response.getBody()).thenReturn(new ByteArrayInputStream("[{\"publishing_status\":\"failed\",\"step\":\"validating\",\"detail\":\"baz\"}]".getBytes(StandardCharsets.UTF_8)));

        final Headers headers = new HeadersImpl();
        headers.setContentType(ContentType.APPLICATION_JSON);
        when(response.getHeaders()).thenReturn(headers);

        when(request.execute()).thenReturn(response);

        assertThrows(EventPublishingException.class, () -> {
            problemHandlingRequest.execute();
        });
    }

}
