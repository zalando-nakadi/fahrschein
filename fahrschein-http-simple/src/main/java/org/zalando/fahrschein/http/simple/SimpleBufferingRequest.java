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

/**
 * {@link Request} implementation that uses standard JDK facilities to
 * execute buffered requests. Created via the {@link SimpleRequestFactory}.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Joern Horstmann
 * @see SimpleRequestFactory#createRequest(java.net.URI, String)
 */
final class SimpleBufferingRequest implements Request {

    private final HttpURLConnection connection;
    private final Headers headers;
    private ByteArrayOutputStream bufferedOutput;
    private boolean executed;

    SimpleBufferingRequest(HttpURLConnection connection) {
        this.connection = connection;
        this.headers = new HeadersImpl();
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
        if (this.headers.getContentLength() < 0) {
            this.headers.setContentLength(size);
        }

        for (String headerName : headers.headerNames()) {
            final List<String> value = headers.get(headerName);
            for (String headerValue : value) {
                final String actualHeaderValue = headerValue != null ? headerValue : "";
                connection.addRequestProperty(headerName, actualHeaderValue);
            }
        }

        // JDK <1.8 doesn't support getOutputStream with HTTP DELETE
        if ("DELETE".equals(getMethod()) && size > 0) {
            this.connection.setDoOutput(false);
        }
        if (this.connection.getDoOutput()) {
            this.connection.setFixedLengthStreamingMode(size);
        }

        this.connection.connect();

        if (this.connection.getDoOutput() && this.bufferedOutput != null) {
            this.bufferedOutput.writeTo(this.connection.getOutputStream());
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
    protected void assertNotExecuted() {
        if (this.executed) {
            throw new IllegalStateException("ClientHttpRequest already executed");
        }
    }
}
