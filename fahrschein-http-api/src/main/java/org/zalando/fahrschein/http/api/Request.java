package org.zalando.fahrschein.http.api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

public interface Request {
    String getMethod();

    URI getURI();

    Headers getHeaders();

    OutputStream getBody() throws IOException;

    Response execute(List<RequestHandler> requestHandlers) throws IOException;

}
