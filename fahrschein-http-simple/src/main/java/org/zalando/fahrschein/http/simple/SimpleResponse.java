package org.zalando.fahrschein.http.simple;

import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.HeadersImpl;
import org.zalando.fahrschein.http.api.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * {@link Response} implementation that uses standard JDK facilities.
 * Obtained via {@link SimpleBufferingRequest#execute()}.
 *
 * @author Arjen Poutsma
 * @author Brian Clozel
 * @author Joern Horstmann
 */
final class SimpleResponse implements Response {

    private final HttpURLConnection connection;
    private Headers headers;
    private InputStream responseStream;

    SimpleResponse(HttpURLConnection connection) {
        this.connection = connection;
    }

    @Override
    public int getStatusCode() throws IOException {
        return this.connection.getResponseCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return this.connection.getResponseMessage();
    }

    @Override
    public Headers getHeaders() {
        if (this.headers == null) {
            final Headers headers = new HeadersImpl();
            // Header field 0 is the status line for most HttpURLConnections, but not on GAE
            String name = connection.getHeaderFieldKey(0);
            if (name != null && name.length() > 0) {
                headers.add(name, connection.getHeaderField(0));
            }
            int i = 1;
            while (true) {
                name = connection.getHeaderFieldKey(i);
                if (name == null || name.length() == 0) {
                    break;
                }
                headers.add(name, this.connection.getHeaderField(i));
                i++;
            }

            this.headers = new HeadersImpl(headers, true);
        }
        return this.headers;
    }

    @Override
    public InputStream getBody() throws IOException {
        if (this.responseStream == null) {
            final InputStream errorStream = connection.getErrorStream();
            this.responseStream = (errorStream != null ? errorStream : connection.getInputStream());
        }
        return this.responseStream;
    }

    @Override
    public void close() {
        if (this.responseStream != null) {
            try {
                this.responseStream.close();
            }
            catch (IOException ex) {
                // ignore
            }
        }
    }

}
