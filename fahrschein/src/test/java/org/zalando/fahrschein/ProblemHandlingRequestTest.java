package org.zalando.fahrschein;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.mockito.Mockito.when;

public class ProblemHandlingRequestTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final Request request = Mockito.mock(Request.class);
    private final Response response = Mockito.mock(Response.class);
    private final ProblemHandlingRequest problemHandlingRequest = new ProblemHandlingRequest(request);

    @Test
    public void shouldCreateProblemFromStatusAndText() throws IOException {
        when(response.getStatusCode()).thenReturn(409);
        when(response.getStatusText()).thenReturn("conflict");

        final Headers headers = new HeadersImpl();
        headers.setContentType(ContentType.TEXT_PLAIN);
        when(response.getHeaders()).thenReturn(headers);

        when(request.execute()).thenReturn(response);

        expectedException.expect(instanceOf(IOProblem.class));
        expectedException.expect(hasFeature("status code", IOProblem::getStatusCode, equalTo(409)));
        expectedException.expect(hasFeature("type", IOProblem::getType, equalTo(URI.create("about:blank"))));
        expectedException.expect(hasFeature("title", IOProblem::getTitle, equalTo("conflict")));
        expectedException.expect(hasFeature("detail", IOProblem::getDetail, equalTo(Optional.<String>empty())));

        problemHandlingRequest.execute();
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

        expectedException.expect(instanceOf(IOProblem.class));
        expectedException.expect(hasFeature("status code", IOProblem::getStatusCode, equalTo(404)));
        expectedException.expect(hasFeature("type", IOProblem::getType, equalTo(URI.create("http://httpstatus.es/404"))));
        expectedException.expect(hasFeature("title", IOProblem::getTitle, equalTo("Not Found")));
        expectedException.expect(hasFeature("detail", IOProblem::getDetail, equalTo(Optional.of("EventType does not exist."))));

        problemHandlingRequest.execute();
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

        expectedException.expect(instanceOf(IOProblem.class));
        expectedException.expect(hasFeature("status code", IOProblem::getStatusCode, equalTo(400)));
        expectedException.expect(hasFeature("type", IOProblem::getType, equalTo(URI.create("about:blank"))));
        expectedException.expect(hasFeature("title", IOProblem::getTitle, equalTo("invalid_request")));
        expectedException.expect(hasFeature("detail", IOProblem::getDetail, equalTo(Optional.of("Access Token not valid"))));

        problemHandlingRequest.execute();
    }

}
