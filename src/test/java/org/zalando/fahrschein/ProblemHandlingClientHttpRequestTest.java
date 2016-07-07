package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProblemHandlingClientHttpRequestTest {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    @Test
    public void recognisesJsonAndProblemBodies() throws IOException {
        final ClientHttpRequest clientHttpRequest = mock(ClientHttpRequest.class);

        when(clientHttpRequest.execute()).thenReturn(createResponse());

        final ProblemHandlingClientHttpRequest problemHandlingClientHttpRequest =
                new ProblemHandlingClientHttpRequest(clientHttpRequest, new ObjectMapper());

        Exception actualException = null;
        try {
            problemHandlingClientHttpRequest.execute();
            Assert.fail("No exception was thrown.");
        } catch (Exception e) {
            actualException = e;
        }

        assertThat("Expected different exception type.", actualException, instanceOf(IOProblem.class));

        final IOProblem ioProblem = (IOProblem) actualException;

        assertThat(ioProblem.getTitle(), equalTo("Not Found"));
        assertThat(ioProblem.getDetail().get(), equalTo("EventType does not exist."));
    }

    private ClientHttpResponse createResponse() {
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.BAD_REQUEST;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return 400;
            }

            @Override
            public String getStatusText() throws IOException {
                return "Bad Request";
            }

            @Override
            public void close() {
                // do nothing
            }

            @Override
            public InputStream getBody() throws IOException {
                return new ByteArrayInputStream("{\"type\":\"http://httpstatus.es/404\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"EventType does not exist.\"}".getBytes(UTF8));
            }

            @Override
            public HttpHeaders getHeaders() {
                final HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add("Content-type", "application/problem+json;charset=UTF-8");
                return httpHeaders;
            }
        };
    }


}