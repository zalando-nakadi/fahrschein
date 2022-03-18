package org.zalando.fahrschein.http.simple;

import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.HeadersImpl;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * {@link Request} implementation that uses standard JDK facilities to
 * execute buffered requests. Created via the {@link SimpleRequestFactory}.
 *
 * See original
 * <a href="https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/http/client/SimpleBufferingClientHttpRequest.java">code from Spring Framework</a>.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Joern Horstmann
 * @see SimpleRequestFactory#createRequest(java.net.URI, String)
 */
final class SimpleBufferingRequest implements Request {

    private final HttpURLConnection connection;
    private final Headers headers;
    private final boolean compressEntity;
    private ByteArrayOutputStream bufferedOutput;
    private boolean executed;

    SimpleBufferingRequest(HttpURLConnection connection, boolean enableContentCompression) {
        this.connection = connection;
        this.headers = new HeadersImpl();
        this.compressEntity = enableContentCompression &&
                this.connection.getRequestProperty("Content-Encoding") == null &&
                this.connection.getDoOutput();
    }

    @Override
    public String getMethod() {
        return this.connection.getRequestMethod();
    }

    @Override
    public URI getURI() {
        try {
            return this.connection.getURL().toURI();
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Could not get HttpURLConnection URI: " + ex.getMessage(), ex);
        }
    }

    private Response executeInternal() throws IOException {
        final int size = this.bufferedOutput != null ? this.bufferedOutput.size() : 0;

        final long contentLength = this.headers.getContentLength();
        if (contentLength >= 0 && contentLength != size) {
            throw new IllegalStateException("Invalid Content-Length header [" + contentLength + "], request size is [" + size + "]");
        }

        for (String headerName : headers.headerNames()) {
            if (!Headers.CONTENT_LENGTH.equalsIgnoreCase(headerName)) {
                final List<String> value = headers.get(headerName);
                for (String headerValue : value) {
                    final String actualHeaderValue = headerValue != null ? headerValue : "";
                    connection.addRequestProperty(headerName, actualHeaderValue);
                }
            }
        }

        // allow gzip-compression from server response
        if (connection.getRequestProperty("Accept-Encoding") == null) {
            connection.setRequestProperty("Accept-Encoding", "gzip");
        }

        if (this.connection.getDoOutput()) {
            this.connection.setFixedLengthStreamingMode(size);
        }

        this.connection.connect();

        if (this.connection.getDoOutput() && size > 0) {
            try (final OutputStream out = this.connection.getOutputStream()) {
                this.bufferedOutput.writeTo(out);
            }
        } else {
            // Immediately trigger the request in a no-output scenario as well
            this.connection.getResponseCode();
        }

        final Response result = new SimpleResponse(this.connection);
        this.bufferedOutput = null;
        return result;
    }

    @Override
    public final Headers getHeaders() {
        return (this.executed ? new HeadersImpl(this.headers, true) : this.headers);
    }

    @Override
    public final OutputStream getBody() throws IOException {
        assertNotExecuted();
        if (this.bufferedOutput == null) {
            this.bufferedOutput = new ByteArrayOutputStream(1024);
            if (this.compressEntity) {
                connection.setRequestProperty("Content-Encoding", "gzip");
                return new GZIPOutputStream(this.bufferedOutput);
            }
        }
        return this.bufferedOutput;
    }

    @Override
    public final Response execute() throws IOException {
        assertNotExecuted();
        final Response result = executeInternal();
        this.executed = true;
        return result;
    }

    /**
     * Assert that this request has not been {@linkplain #execute() executed} yet.
     * @throws IllegalStateException if this request has been executed
     */
    private void assertNotExecuted() {
        if (this.executed) {
            throw new IllegalStateException("Request already executed");
        }
    }
}
