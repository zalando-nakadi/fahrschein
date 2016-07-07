package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.problem.MoreStatus;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Arrays.asList;

public class ProblemHandlingClientHttpRequest implements ClientHttpRequest {

    private static final MediaType APPLICATION_PROBLEM_JSON = new MediaType("application", "problem+json");
    private static final Set<MediaType> PROBLEM_CONTENT_TYPES = new HashSet<>(asList(APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON));
    private static final URI DEFAULT_PROBLEM_TYPE = URI.create("about:blank");

    private static final Map<Integer, Response.StatusType> STATUS_BY_CODE = new HashMap<>();
    static {
        for (MoreStatus status : MoreStatus.values()) {
            STATUS_BY_CODE.put(status.getStatusCode(), status);
        }

        for (Response.Status status : Response.Status.values()) {
            STATUS_BY_CODE.put(status.getStatusCode(), status);
        }
    }

    private final ClientHttpRequest clientHttpRequest;
    private final ObjectMapper objectMapper;

    public ProblemHandlingClientHttpRequest(ClientHttpRequest clientHttpRequest) {
        this.clientHttpRequest = clientHttpRequest;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ClientHttpResponse execute() throws IOException {
        final ClientHttpResponse response = clientHttpRequest.execute();

        final int statusCode = response.getRawStatusCode();
        if (statusCode >= 400) {
            final String statusText = response.getStatusText();
            final MediaType contentType = response.getHeaders().getContentType();
            if (contentType != null && isProblemContentType(contentType)) {
                try (InputStream is = response.getBody()) {
                    final IOProblem problem = deserializeProblem(is, statusCode, statusText);
                    if (problem != null) {
                        throw problem;
                    } else {
                        throw new IOProblem(DEFAULT_PROBLEM_TYPE, statusText, STATUS_BY_CODE.get(statusCode));
                    }
                }
            } else {
                throw new IOProblem(DEFAULT_PROBLEM_TYPE, statusText, STATUS_BY_CODE.get(statusCode));
            }
        }

        return response;
    }

    private boolean isProblemContentType(final MediaType contentType) {
        for (MediaType problemContentType : PROBLEM_CONTENT_TYPES) {
            if (Objects.equals(problemContentType.getType(), contentType.getType())
                    && Objects.equals(problemContentType.getSubtype(), contentType.getSubtype())) {
                return true;
            }
        }

        return false;
    }

    private @Nullable IOProblem deserializeProblem(final InputStream is, final int statusCode, final String statusText) throws IOException {
        final JsonNode rootNode = objectMapper.readTree(is);

        final JsonNode typeNode = rootNode.get("type");
        final JsonNode titleNode = rootNode.get("title");

        if (typeNode != null && titleNode != null) {
            final String type = typeNode.asText();
            final String title = titleNode.asText();
            final int status = rootNode.get("status").asInt();

            final JsonNode detailNode = rootNode.get("detail");
            final String detail = detailNode == null ? null : detailNode.asText(null);

            final JsonNode instanceNode = rootNode.get("instance");
            final String instance = instanceNode == null ? null : instanceNode.asText(null);
            final URI instanceURI = instance == null ? null : URI.create(instance);

            return new IOProblem(URI.create(type), title, STATUS_BY_CODE.get(status), detail, instanceURI);
        } else {
            final JsonNode errorNode = rootNode.get("error");
            final JsonNode descriptionNode = rootNode.get("error_description");
            if (errorNode != null && descriptionNode != null) {
                final String error = errorNode.asText();
                final String description = descriptionNode.asText();

                return new IOProblem(DEFAULT_PROBLEM_TYPE, error, STATUS_BY_CODE.get(statusCode), description);
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
