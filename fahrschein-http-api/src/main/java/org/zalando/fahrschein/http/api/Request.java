package org.zalando.fahrschein.http.api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public interface Request {
    String getMethod();

    URI getURI();

    Headers getHeaders();

    OutputStream getBody() throws IOException;

    Response execute() throws IOException;

}
