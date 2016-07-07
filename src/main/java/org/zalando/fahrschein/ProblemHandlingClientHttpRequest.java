package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
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
import static org.springframework.util.ObjectUtils.nullSafeEquals;

public class ProblemHandlingClientHttpRequest implements ClientHttpRequest {

    public static final MediaType APPLICATION_PROBLEM_JSON = new MediaType("application", "problem+json");
    private static final Set<MediaType> PROBLEM_CONTENT_TYPES = new HashSet<>(asList(APPLICATION_PROBLEM_JSON));
    private static final URI DEFAULT_PROBLEM_TYPE = URI.create("about:blank");

    private final ClientHttpRequest clientHttpRequest;
    private final ObjectMapper objectMapper = createObjectMapper();

    public ProblemHandlingClientHttpRequest(ClientHttpRequest clientHttpRequest, ObjectMapper objectMapper) {
        this.clientHttpRequest = clientHttpRequest;
    }

    @Override
    public ClientHttpResponse execute() throws IOException {
        final ClientHttpResponse response = clientHttpRequest.execute();

        final HttpStatus statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {

            final MediaType contentType = response.getHeaders().getContentType();
            if (isProblemContentType(contentType)) {
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

    private boolean isProblemContentType(final MediaType contentType) {
        if (contentType == null) {
            return false;
        }

        for (MediaType problemContentType : PROBLEM_CONTENT_TYPES) {
            if (nullSafeEquals(problemContentType.getType(), contentType.getType())
                    && nullSafeEquals(problemContentType.getSubtype(), contentType.getSubtype())) {
                return true;
            }
        }

        return false;
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

    private static ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.registerModule(new Jdk8Module());
        return objectMapper;
    }

}
