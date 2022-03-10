package org.zalando.fahrschein.http.api;

import java.io.IOException;
import java.net.URI;

public interface RequestFactory {

    /**
     * By default, POST-content is gzip-compressed. This disables content compression.
     */
    void disableContentCompression();

    /**
     * Creates a new request using the underlying RequestFactory implementation.
     * @param uri request target URI
     * @param method request method (GET, POST, ...)
     * @return the request
     * @throws IOException in case of I/O issues while trying to create the request.
     */
    Request createRequest(URI uri, String method) throws IOException;

}
