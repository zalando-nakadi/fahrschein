package org.zalando.fahrschein.http.jdk11;

import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class JavaNetBufferingRequest implements Request {

    private final HttpRequest.Builder request;
    private final HttpClient client;
    private final URI uri;
    private final String method;
    private final ContentEncoding contentEncoding;
    private final Optional<Duration> requestTimeout;
    private boolean executed;
    private ByteArrayOutputStream bufferedOutput;

    JavaNetBufferingRequest(URI uri, String method, HttpClient client, Optional<Duration> requestTimeout, ContentEncoding contentEncoding) {
        this.uri = uri;
        this.method = method;
        this.request = HttpRequest.newBuilder().header("Accept-Encoding", "gzip");
        this.client = client;
        this.requestTimeout = requestTimeout;
        this.contentEncoding = contentEncoding;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public Headers getHeaders() {
        return new Headers() {
            @Override
            public List<String> get(String headerName) {
                return List.of();
            }

            @Override
            public void add(String headerName, String value) {
                request.header(headerName, value);
            }

            @Override
            public void put(String headerName, String value) {
                request.header(headerName, value);
            }

            @Override
            public String getFirst(String headerName) {
                return "";
            }

            @Override
            public Set<String> headerNames() {
                return Set.of();
            }

            @Override
            public long getContentLength() {
                return 0;
            }

            @Override
            public void setContentLength(long contentLength) {
                // NO-OP.
            }

            @Override
            public ContentType getContentType() {
                return ContentType.APPLICATION_JSON;
            }

            @Override
            public void setContentType(ContentType contentType) {
                request.header("Content-Type", contentType.getValue());
            }
        };
    }

    @Override
    public OutputStream getBody() throws IOException {
        if (this.bufferedOutput == null) {
            this.bufferedOutput = new ByteArrayOutputStream(1024);
            if (this.contentEncoding.isSupported(getMethod())) {
                // probably premature optimization, but we're omitting the unnecessary
                // "Content-Encoding: identity" header
                if (ContentEncoding.IDENTITY != this.contentEncoding) {
                    request.setHeader("Content-Encoding", this.contentEncoding.value());
                }
                return this.contentEncoding.wrap(this.bufferedOutput);
            }
        }
        return this.bufferedOutput;
    }

    @Override
    public Response execute() throws IOException {
        try {
            requestTimeout.ifPresent(t -> request.timeout(t));
            HttpResponse<InputStream> response = client.send(
                    request
                            .uri(this.uri)
                            .method(this.method,
                                    this.bufferedOutput == null
                                            ? HttpRequest.BodyPublishers.noBody()
                                            : HttpRequest.BodyPublishers.ofByteArray(this.bufferedOutput.toByteArray()))
                            .build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            this.executed = true;
            return new JavaNetResponse(response);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private void assertNotExecuted() {
        if (this.executed) {
            throw new IllegalStateException("Request already executed");
        }
    }
}
