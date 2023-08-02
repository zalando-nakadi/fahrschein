package org.zalando.spring.boot.fahrschein.nakadi.config;

import org.zalando.fahrschein.AccessTokenProvider;
import org.zalando.fahrschein.PlatformAccessTokenProvider;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.OAuthConfig;

import java.nio.file.Paths;

final
class OAuth {
    private OAuth() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static AccessTokenProvider buildAccessTokenProvider(OAuthConfig oauthConfig) {
        if (oauthConfig.getCredentialsDirectory() == null && oauthConfig.getAccessTokenId() == null) {
            return new PlatformAccessTokenProvider();
        }

        if (oauthConfig.getCredentialsDirectory() == null) {
            return new PlatformAccessTokenProvider(oauthConfig.getAccessTokenId());
        }

        return new PlatformAccessTokenProvider(
                Paths.get(oauthConfig.getCredentialsDirectory()),
                oauthConfig.getAccessTokenId());
    }

}
