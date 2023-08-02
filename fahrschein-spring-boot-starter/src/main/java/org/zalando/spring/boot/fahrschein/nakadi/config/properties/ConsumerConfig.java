package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.zalando.spring.boot.fahrschein.nakadi.config.properties.Merger.merge;

@Validated
public class ConsumerConfig extends AbstractConfig {

    private StreamParametersConfig streamParameters = new StreamParametersConfig();

    private List<String> topics = new ArrayList<>();

    public ConsumerConfig() {
    }

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
        if (defaultConsumerConfig.getOauth().getEnabled() && !this.getOauth().getEnabled()) {
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

    public StreamParametersConfig getStreamParameters() {
        return this.streamParameters;
    }

    public List<String> getTopics() {
        return this.topics;
    }

    public void setStreamParameters(StreamParametersConfig streamParameters) {
        this.streamParameters = streamParameters;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public String toString() {
        return "ConsumerConfig(streamParameters=" + this.getStreamParameters() + ", topics=" + this.getTopics() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ConsumerConfig)) return false;
        final ConsumerConfig other = (ConsumerConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$streamParameters = this.getStreamParameters();
        final Object other$streamParameters = other.getStreamParameters();
        if (this$streamParameters == null ? other$streamParameters != null : !this$streamParameters.equals(other$streamParameters))
            return false;
        final Object this$topics = this.getTopics();
        final Object other$topics = other.getTopics();
        if (this$topics == null ? other$topics != null : !this$topics.equals(other$topics)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ConsumerConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $streamParameters = this.getStreamParameters();
        result = result * PRIME + ($streamParameters == null ? 43 : $streamParameters.hashCode());
        final Object $topics = this.getTopics();
        result = result * PRIME + ($topics == null ? 43 : $topics.hashCode());
        return result;
    }
}