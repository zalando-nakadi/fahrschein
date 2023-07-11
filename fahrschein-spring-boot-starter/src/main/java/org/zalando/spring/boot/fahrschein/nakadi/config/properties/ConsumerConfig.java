package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.zalando.spring.boot.fahrschein.nakadi.config.properties.Merger.merge;

@Data
@Validated
@EqualsAndHashCode(callSuper = false) 
public class ConsumerConfig extends AbstractConfig {

    private StreamParametersConfig streamParameters = new StreamParametersConfig();

    private List<String> topics = new ArrayList<>();

    void mergeWithDefaultConfig(DefaultConsumerConfig defaultConsumerConfig) {
        this.setApplicationName(merge(this.getApplicationName(), defaultConsumerConfig.getApplicationName()));
        this.setApplicationName(ofNullable(this.getApplicationName()).orElse(defaultConsumerConfig.getApplicationName()));
        this.setConsumerGroup(ofNullable(this.getConsumerGroup()).orElse(defaultConsumerConfig.getConsumerGroup()));
        this.setNakadiUrl(ofNullable(this.getNakadiUrl()).orElse(defaultConsumerConfig.getNakadiUrl()));
        this.setAutostartEnabled(ofNullable(this.getAutostartEnabled()).orElse(defaultConsumerConfig.getAutostartEnabled()));
        this.setReadFrom(ofNullable(this.getReadFrom()).orElse(defaultConsumerConfig.getReadFrom()));
        this.setRecordMetrics(ofNullable(this.getRecordMetrics()).orElse(defaultConsumerConfig.getRecordMetrics()));
        this.setObjectMapperRef(ofNullable(this.getObjectMapperRef()).orElse(defaultConsumerConfig.getObjectMapperRef()));

        // oauth
        if(defaultConsumerConfig.getOauth().getEnabled() && !this.getOauth().getEnabled()) {
            this.oauth.setEnabled(defaultConsumerConfig.getOauth().getEnabled());
            this.oauth.setAccessTokenIdIfNotConfigured(defaultConsumerConfig.getOauth().getAccessTokenId());
            this.oauth.setCredentialsDirectoryIfNotConfigured(defaultConsumerConfig.getOauth().getCredentialsDirectory());
        }

        this.getHttp().mergeFromDefaults(defaultConsumerConfig.getHttp());

        this.getBackoff().mergeFromDefaults(defaultConsumerConfig.getBackoff());

        this.getAuthorizations().mergeFromDefaults(defaultConsumerConfig.getAuthorizations());

        this.getStreamParameters().mergeFromDefaults(defaultConsumerConfig.getStreamParameters());

        this.getThreads().setListenerPoolSize(Math.max(this.getThreads().getListenerPoolSize(), defaultConsumerConfig.getThreads().getListenerPoolSize()));
    }

}