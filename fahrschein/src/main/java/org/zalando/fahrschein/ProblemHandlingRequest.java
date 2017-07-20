package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.Response;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;


class ProblemHandlingRequest implements Request {

    private static final URI DEFAULT_PROBLEM_TYPE = URI.create("about:blank");

    private final Request request;
    private final ObjectMapper objectMapper;

    public ProblemHandlingRequest(Request request) {
        this.request = request;
        this.objectMapper = DefaultObjectMapper.INSTANCE;
    }

    @Override
    public Response execute() throws IOException {
        final Response response = request.execute();

        try {
            final int statusCode = response.getStatusCode();
            if (statusCode >= 400 && statusCode != 422) {
                final String statusText = response.getStatusText();

                final ContentType contentType = response.getHeaders().getContentType();
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

    private static boolean isProblem(ContentType contentType) {
        return ContentType.APPLICATION_JSON.getType().equals(contentType.getType()) && ContentType.APPLICATION_JSON.getSubtype().equals(contentType.getSubtype())
                || ContentType.APPLICATION_PROBLEM_JSON.getType().equals(contentType.getType()) && ContentType.APPLICATION_PROBLEM_JSON.getSubtype().equals(contentType.getSubtype());
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
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public URI getURI() {
        return request.getURI();
    }

    @Override
    public Headers getHeaders() {
        return request.getHeaders();
    }

    @Override
    public OutputStream getBody() throws IOException {
        return request.getBody();
    }

}
