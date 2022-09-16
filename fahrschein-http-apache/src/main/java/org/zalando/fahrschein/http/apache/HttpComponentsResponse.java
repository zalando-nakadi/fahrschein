package org.zalando.fahrschein.http.apache;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.HeadersImpl;
import org.zalando.fahrschein.http.api.Response;
import org.zalando.fahrschein.http.api.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link Response} implementation based on Apache HttpComponents HttpClient.
 *
 * <p>Created via the {@link HttpComponentsRequest}.
 *
 * @author Oleg Kalnichevski
 * @author Arjen Poutsma
 * @author Joern Horstmann
 * @see HttpComponentsRequest#execute()
 */
final class HttpComponentsResponse implements Response {

    private final HttpResponse httpResponse;
    private Headers headers;
    private InputStream responseStream;

    HttpComponentsResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    @Override
    public int getStatusCode() throws IOException {
        return this.httpResponse.getStatusLine().getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return this.httpResponse.getStatusLine().getReasonPhrase();
    }

    @Override
    public Headers getHeaders() {
        if (this.headers == null) {
            this.headers = new HeadersImpl();
            for (Header header : this.httpResponse.getAllHeaders()) {
                this.headers.add(header.getName(), header.getValue());
            }
        }
        return this.headers;
    }

    @Override
    public InputStream getBody() throws IOException {
        if (this.responseStream == null) {
            HttpEntity entity = this.httpResponse.getEntity();
            this.responseStream = (entity != null ? entity.getContent() : new ByteArrayInputStream(new byte[0]));
        }
        return this.responseStream;
    }

    @Override
    public void close() {
        // Release underlying connection back to the connection manager
        try {
            if (this.responseStream != null) {
                StreamUtils.drain(this.responseStream);
                this.responseStream.close();
            }
            if (this.httpResponse instanceof Closeable) {
                ((Closeable) this.httpResponse).close();
            }
        } catch (IOException e) {
            // ignore exception on close
        }
    }

}
