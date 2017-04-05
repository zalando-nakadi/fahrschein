package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;

class ProblemHandlingClientHttpRequest implements ClientHttpRequest {

    private static final URI DEFAULT_PROBLEM_TYPE = URI.create("about:blank");
    private static final MediaType APPLICATION_PROBLEM_JSON = MediaType.parseMediaType("application/problem+json");

    private final ClientHttpRequest clientHttpRequest;
    private final ObjectMapper objectMapper;

    public ProblemHandlingClientHttpRequest(ClientHttpRequest clientHttpRequest) {
        this.clientHttpRequest = clientHttpRequest;
        this.objectMapper = DefaultObjectMapper.INSTANCE;
    }

    @Override
    public ClientHttpResponse execute() throws IOException {
        final ClientHttpResponse response = clientHttpRequest.execute();

        try {
            final int statusCode = response.getRawStatusCode();
            if (statusCode >= 400 && statusCode != 422) {
                final String statusText = response.getStatusText();

                final MediaType contentType = response.getHeaders().getContentType();
                if (isProblem(contentType)) {
                    try (final InputStream is = response.getBody()) {
                        final IOProblem problem = deserializeProblem(is, statusCode);
                        if (problem != null) {
                            throw problem;
                        } else {
                            throw new IOProblem(DEFAULT_PROBLEM_TYPE, statusText, statusCode);
                        }
                    }
                } else {
                    throw new IOProblem(DEFAULT_PROBLEM_TYPE, statusText, statusCode);
                }
            }
        } catch (Throwable throwable) {
            try {
                response.close();
            } catch (Throwable suppressed) {
                throwable.addSuppressed(suppressed);
            }
            throw throwable;
        }

        return response;
    }

    private static boolean isProblem(MediaType contentType) {
        return APPLICATION_JSON.getType().equals(contentType.getType()) && APPLICATION_JSON.getSubtype().equals(contentType.getSubtype())
                || APPLICATION_PROBLEM_JSON.getType().equals(contentType.getType()) && APPLICATION_PROBLEM_JSON.getSubtype().equals(contentType.getSubtype());
    }

    private @Nullable IOProblem deserializeProblem(final InputStream is, final int statusCode) throws IOException {
        final JsonNode rootNode = objectMapper.readTree(is);

        final JsonNode typeNode = rootNode.get("type");
        final JsonNode titleNode = rootNode.get("title");

        if (typeNode != null && titleNode != null) {
            final String type = typeNode.asText();
            final String title = titleNode.asText();

            final JsonNode detailNode = rootNode.get("detail");
            final String detail = detailNode == null ? null : detailNode.asText(null);

            final JsonNode instanceNode = rootNode.get("instance");
            final String instance = instanceNode == null ? null : instanceNode.asText(null);

            return new IOProblem(URI.create(type), title, statusCode, detail, instance == null ? null : URI.create(instance));
        } else {
            final JsonNode errorNode = rootNode.get("error");
            final JsonNode descriptionNode = rootNode.get("error_description");

            if (errorNode != null && descriptionNode != null) {
                final String error = errorNode.asText();
                final String description = descriptionNode.asText();

                return new IOProblem(DEFAULT_PROBLEM_TYPE, error, statusCode, description);
            } else {
                return null;
            }
        }
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
