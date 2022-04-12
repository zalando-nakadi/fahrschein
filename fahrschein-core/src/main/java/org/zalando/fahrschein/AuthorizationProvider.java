package org.zalando.fahrschein;

import java.io.IOException;

public interface AuthorizationProvider {
    String getAuthorizationHeader() throws IOException;
}
