package org.zalando.fahrschein;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlatformAccessTokenProviderTest {

    @Test
    public void shouldLoadAuthorizationToken() throws IOException {
        final PlatformAccessTokenProvider tokenProvider = new PlatformAccessTokenProvider(Paths.get("./src/test/resources/meta/credentials"), "nakadi");
        final String[] tokenWithType = tokenProvider.getAuthorizationHeader().split(" ");
        assertNotNull(tokenWithType);
        assertEquals("Bearer", tokenWithType[0]);
        assertEquals("some-secret-token", tokenWithType[1]);
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

    @Test
    public void shouldTrimAuthorizationToken() throws IOException {
        final PlatformAccessTokenProvider tokenProvider = new PlatformAccessTokenProvider(Paths.get("./src/test/resources/meta/credentials"), "whitespace-newline");
        final String[] tokenWithType = tokenProvider.getAuthorizationHeader().split(" ");
        assertNotNull(tokenWithType);
        assertEquals("Bearer", tokenWithType[0]);
        assertEquals("some-secret-token", tokenWithType[1]);
    }
}