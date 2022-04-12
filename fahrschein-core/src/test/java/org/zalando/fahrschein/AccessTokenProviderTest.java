package org.zalando.fahrschein;

import org.junit.Test;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class AccessTokenProviderTest {

    @Test
    public void shouldAdaptToBearerAuthorizationProvider() throws IOException {
        final String token = "token";
        final AuthorizationProvider provider = (AccessTokenProvider) () -> token;
        assertThat(provider.getAuthorizationHeader(), equalTo("Bearer " + token));
    }
}