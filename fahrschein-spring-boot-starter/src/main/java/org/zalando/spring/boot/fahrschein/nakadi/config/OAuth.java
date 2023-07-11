package org.zalando.spring.boot.fahrschein.nakadi.config;

import lombok.experimental.UtilityClass;
import org.zalando.fahrschein.AccessTokenProvider;
import org.zalando.fahrschein.PlatformAccessTokenProvider;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.OAuthConfig;

import java.nio.file.Paths;

@UtilityClass
class OAuth {
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
