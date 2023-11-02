package org.zalando.fahrschein.http.apache;

import org.apache.hc.client5.http.classic.HttpClient;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.HeadersImpl;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

/**
 * {@link Request} implementation based on Apache HttpComponents HttpClient.
 *
 * <p>Created via the {@link HttpComponentsRequestFactory}.
 *
 * @author Oleg Kalnichevski
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Joern Horstmann
 * @see HttpComponentsRequestFactory#createRequest(URI, String)
 */
final class HttpComponentsRequest implements Request {

    private final HttpClient httpClient;
    private final ClassicHttpRequest httpRequest;
    private final ContentEncoding contentEncoding;

    private final Headers headers;
    private ByteArrayOutputStream bufferedOutput;
    private boolean executed;

    HttpComponentsRequest(HttpClient client, ClassicHttpRequest request, ContentEncoding contentEncoding) {
        this.httpClient = client;
        this.httpRequest = request;
        this.contentEncoding = contentEncoding;
        this.headers = new HeadersImpl();
    }

    @Override
    public String getMethod() {
        return this.httpRequest.getMethod();
    }

    @Override
    public URI getURI() {
        return URI.create(this.httpRequest.getRequestUri());
    }

    private Response executeInternal(Headers headers) throws IOException {
        final byte[] bytes = this.bufferedOutput != null ? this.bufferedOutput.toByteArray() : new byte[0];

        if (headers.getContentLength() < 0) {
            headers.setContentLength(bytes.length);
        }

        for (String headerName : headers.headerNames()) {
            final List<String> value = headers.get(headerName);
            if (! Headers.CONTENT_LENGTH.equalsIgnoreCase(headerName) && !Headers.TRANSFER_ENCODING.equalsIgnoreCase(headerName)) {
                for (String headerValue : value) {
                    this.httpRequest.addHeader(headerName, headerValue);
                }
            }
        }

        HttpEntity requestEntity = new ByteArrayEntity(bytes, ContentType.APPLICATION_JSON);
        this.httpRequest.setEntity(requestEntity);

        final ClassicHttpResponse httpResponse = (ClassicHttpResponse) this.httpClient.execute(this.httpRequest);
        final Response result = new HttpComponentsResponse(httpResponse);
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
            if (this.contentEncoding.isSupported(getMethod())) {
                // probably premature optimization, but we're omitting the unnecessary
                // "Content-Encoding: identity" header
                if (ContentEncoding.IDENTITY != this.contentEncoding) {
                    this.httpRequest.setHeader(Headers.CONTENT_ENCODING, this.contentEncoding.value());
                }
                return this.contentEncoding.wrap(this.bufferedOutput);
            }
        }
        return this.bufferedOutput;
    }

    @Override
    public final Response execute() throws IOException {
        assertNotExecuted();
        final Response result = executeInternal(this.headers);
        this.executed = true;
        return result;
    }

    /**
     * Assert that this request has not been {@linkplain #execute() executed} yet.
     * @throws IllegalStateException if this request has been executed
     */
    private void assertNotExecuted() {
        if (this.executed) {
            throw new IllegalStateException("ClientHttpRequest already executed");
        }
    }
}
