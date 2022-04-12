package org.zalando.fahrschein;

import java.io.IOException;

public interface AccessTokenProvider extends AuthorizationProvider {
    String getAccessToken() throws IOException;

    @Override
    default String getAuthorizationHeader() throws IOException {
        return "Bearer " + getAccessToken();
    }
}
