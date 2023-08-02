package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import java.util.Optional;

public class PublisherConfig extends AbstractConfig {

    public PublisherConfig() {
        setId("publisher_default");
    }

    void mergeWithDefaultConfig(DefaultConsumerConfig defaultConsumerConfig) {
        this.setNakadiUrl(Optional.ofNullable(this.getNakadiUrl()).orElse(defaultConsumerConfig.getNakadiUrl()));
        this.setApplicationName(Optional.ofNullable(this.getApplicationName()).orElse(defaultConsumerConfig.getApplicationName()));

        this.setObjectMapperRef(Optional.ofNullable(this.getObjectMapperRef()).orElse(defaultConsumerConfig.getObjectMapperRef()));

        // oauth
        if (defaultConsumerConfig.getOauth().getEnabled() && !this.getOauth().getEnabled()) {
            this.oauth.setEnabled(defaultConsumerConfig.getOauth().getEnabled());
            this.oauth.setAccessTokenIdIfNotConfigured(defaultConsumerConfig.getOauth().getAccessTokenId());
            this.oauth.setCredentialsDirectoryIfNotConfigured(defaultConsumerConfig.getOauth().getCredentialsDirectory());
        }

        this.getHttp().mergeFromDefaults(defaultConsumerConfig.getHttp());
    }

    public String toString() {
        return "PublisherConfig()";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PublisherConfig)) return false;
        final PublisherConfig other = (PublisherConfig) o;
        if (!other.canEqual((Object) this)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PublisherConfig;
    }

    public int hashCode() {
        int result = 1;
        return result;
    }
}
