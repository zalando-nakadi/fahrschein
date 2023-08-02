package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import java.util.Optional;

public class OAuthConfig {

    private Boolean enabled;

    private String accessTokenId;

    private String credentialsDirectory;

    public OAuthConfig() {
    }

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

    public Boolean getEnabled() {
        return this.enabled;
    }

    public String getAccessTokenId() {
        return this.accessTokenId;
    }

    public String getCredentialsDirectory() {
        return this.credentialsDirectory;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setAccessTokenId(String accessTokenId) {
        this.accessTokenId = accessTokenId;
    }

    public void setCredentialsDirectory(String credentialsDirectory) {
        this.credentialsDirectory = credentialsDirectory;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof OAuthConfig)) return false;
        final OAuthConfig other = (OAuthConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$enabled = this.getEnabled();
        final Object other$enabled = other.getEnabled();
        if (this$enabled == null ? other$enabled != null : !this$enabled.equals(other$enabled)) return false;
        final Object this$accessTokenId = this.getAccessTokenId();
        final Object other$accessTokenId = other.getAccessTokenId();
        if (this$accessTokenId == null ? other$accessTokenId != null : !this$accessTokenId.equals(other$accessTokenId))
            return false;
        final Object this$credentialsDirectory = this.getCredentialsDirectory();
        final Object other$credentialsDirectory = other.getCredentialsDirectory();
        if (this$credentialsDirectory == null ? other$credentialsDirectory != null : !this$credentialsDirectory.equals(other$credentialsDirectory))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof OAuthConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $enabled = this.getEnabled();
        result = result * PRIME + ($enabled == null ? 43 : $enabled.hashCode());
        final Object $accessTokenId = this.getAccessTokenId();
        result = result * PRIME + ($accessTokenId == null ? 43 : $accessTokenId.hashCode());
        final Object $credentialsDirectory = this.getCredentialsDirectory();
        result = result * PRIME + ($credentialsDirectory == null ? 43 : $credentialsDirectory.hashCode());
        return result;
    }

    public String toString() {
        return "OAuthConfig(enabled=" + this.getEnabled() + ", accessTokenId=" + this.getAccessTokenId() + ", credentialsDirectory=" + this.getCredentialsDirectory() + ")";
    }
}
