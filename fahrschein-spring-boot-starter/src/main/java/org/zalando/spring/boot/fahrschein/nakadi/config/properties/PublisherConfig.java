package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
public class PublisherConfig extends AbstractConfig {

    public PublisherConfig() {
        setId("publisher_default");
    }

    void mergeWithDefaultConfig(DefaultConsumerConfig defaultConsumerConfig) {
        this.setNakadiUrl(Optional.ofNullable(this.getNakadiUrl()).orElse(defaultConsumerConfig.getNakadiUrl()));
        this.setApplicationName(Optional.ofNullable(this.getApplicationName()).orElse(defaultConsumerConfig.getApplicationName()));

        this.setObjectMapperRef(Optional.ofNullable(this.getObjectMapperRef()).orElse(defaultConsumerConfig.getObjectMapperRef()));

        // oauth
        if(defaultConsumerConfig.getOauth().getEnabled() && !this.getOauth().getEnabled()) {
            this.oauth.setEnabled(defaultConsumerConfig.getOauth().getEnabled());
            this.oauth.setAccessTokenIdIfNotConfigured(defaultConsumerConfig.getOauth().getAccessTokenId());
            this.oauth.setCredentialsDirectoryIfNotConfigured(defaultConsumerConfig.getOauth().getCredentialsDirectory());
        }

        this.getHttp().mergeFromDefaults(defaultConsumerConfig.getHttp());
    }
}
