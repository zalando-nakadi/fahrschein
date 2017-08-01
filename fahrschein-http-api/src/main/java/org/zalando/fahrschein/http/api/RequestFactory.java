package org.zalando.fahrschein.http.api;

import java.io.IOException;
import java.net.URI;

public interface RequestFactory {

    Request createRequest(URI uri, String method) throws IOException;

}
