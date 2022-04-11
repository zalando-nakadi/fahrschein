package org.zalando.fahrschein.http.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface Response extends Closeable {

    int getStatusCode() throws IOException;

    /**
     * deprecated because with HTTP/2 we will not get any status text anymore.
     */
    @Deprecated
    String getStatusText() throws IOException;

    Headers getHeaders();

    InputStream getBody() throws IOException;

    @Override
    void close();

}
