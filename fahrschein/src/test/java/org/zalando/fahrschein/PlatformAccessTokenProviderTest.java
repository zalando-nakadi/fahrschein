package org.zalando.fahrschein;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class PlatformAccessTokenProviderTest {

    @Test
    public void shouldLoadAuthorizationToken() throws IOException {
        final PlatformAccessTokenProvider tokenProvider = new PlatformAccessTokenProvider(Paths.get("./src/test/resources/meta/credentials"), "nakadi");
        final String[] tokenWithType = tokenProvider.getAuthorizationHeader().split(" ");
        Assertions.assertNotNull(tokenWithType);
        Assertions.assertEquals("Bearer", tokenWithType[0]);
        Assertions.assertEquals("some-secret-token", tokenWithType[1]);
    }

    @Test
    public void shouldThrowExceptionWhenFileDoesntExist() {
        final PlatformAccessTokenProvider tokenProvider = new PlatformAccessTokenProvider(Paths.get("./src/test/resources/meta/credentials"), "dummy");
        Assertions.assertThrows(IOException.class, tokenProvider::getAuthorizationHeader);
    }

    @Test
    public void shouldThrowExceptionWhenSecretFileIsEmpty() {
        final PlatformAccessTokenProvider tokenProvider = new PlatformAccessTokenProvider(Paths.get("./src/test/resources/meta/credentials"), "empty");
        Assertions.assertThrows(IllegalArgumentException.class, tokenProvider::getAuthorizationHeader);
    }
}
