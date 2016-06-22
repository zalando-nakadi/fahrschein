package org.zalando.fahrschein;

import java.io.IOException;

public interface AccessTokenProvider {
    String getAccessToken() throws IOException;
}
