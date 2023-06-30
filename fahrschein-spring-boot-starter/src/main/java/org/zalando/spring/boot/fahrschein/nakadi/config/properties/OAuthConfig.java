package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.Data;

import java.util.Optional;

@Data
public class OAuthConfig {

    private Boolean enabled;

    private String accessTokenId;

    private String credentialsDirectory;

    public static OAuthConfig defaultOAuthConfig() {
        OAuthConfig c = new OAuthConfig();
        c.setEnabled(Boolean.FALSE);

        return c;
    }

    public void setAccessTokenIdIfNotConfigured(String key) {
        this.setAccessTokenId(Optional.ofNullable(this.getAccessTokenId()).orElse(key));
    }

    public void setCredentialsDirectoryIfNotConfigured(String credentialsDirectory) {
        this.setCredentialsDirectory(Optional.ofNullable(this.getCredentialsDirectory()).orElse(credentialsDirectory));
    }

}
