package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class ProblemHandlingClientHttpRequest implements ClientHttpRequest {

    private static final Set<String> PROBLEM_CONTENT_TYPES = new HashSet<>(asList("application/json", "application/problem+json"));
    private static final URI DEFAULT_PROBLEM_TYPE = URI.create("about:blank");

    private final ClientHttpRequest clientHttpRequest;
    private final ObjectMapper objectMapper;

    public ProblemHandlingClientHttpRequest(ClientHttpRequest clientHttpRequest, ObjectMapper objectMapper) {
        this.clientHttpRequest = clientHttpRequest;
        this.objectMapper = objectMapper;
    }

    @Override
    public ClientHttpResponse execute() throws IOException {
        final ClientHttpResponse response = clientHttpRequest.execute();

        final HttpStatus statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {

            final MediaType contentType = response.getHeaders().getContentType();
            if (PROBLEM_CONTENT_TYPES.contains(contentType.getType())) {
                try (InputStream is = response.getBody()) {
                    final IOProblem problem = objectMapper.readValue(is, IOProblem.class);
                    if (problem != null) {
                        throw problem;
                    } else {
                        throw new IOProblem(DEFAULT_PROBLEM_TYPE, statusCode.getReasonPhrase(), statusCode.value());
                    }
                }
            } else {
                throw new IOProblem(DEFAULT_PROBLEM_TYPE, statusCode.getReasonPhrase(), statusCode.value());
            }
        }

        return response;
    }

    @Override
    public HttpMethod getMethod() {
        return clientHttpRequest.getMethod();
    }

    @Override
    public URI getURI() {
        return clientHttpRequest.getURI();
    }

    @Override
    public HttpHeaders getHeaders() {
        return clientHttpRequest.getHeaders();
    }

    @Override
    public OutputStream getBody() throws IOException {
        return clientHttpRequest.getBody();
    }
}
