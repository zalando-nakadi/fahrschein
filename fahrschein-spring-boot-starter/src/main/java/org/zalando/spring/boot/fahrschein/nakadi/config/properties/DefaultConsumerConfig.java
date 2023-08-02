package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.springframework.validation.annotation.Validated;

@Validated
public class DefaultConsumerConfig extends AbstractConfig {

    public static final String FAHRSCHEIN_OBJECT_MAPPER_REF_NAME = "fahrscheinObjectMapper";
    private StreamParametersConfig streamParameters = new StreamParametersConfig();

    public DefaultConsumerConfig() {
        super();
        setHttp(HttpConfig.defaultHttpConfig());
        setId("CONFIG_DEFAULT");
        setAutostartEnabled(Boolean.TRUE);
        setReadFrom(Position.END);
        setRecordMetrics(Boolean.FALSE);
        setObjectMapperRef(FAHRSCHEIN_OBJECT_MAPPER_REF_NAME);
    }

    public StreamParametersConfig getStreamParameters() {
        return this.streamParameters;
    }

    public void setStreamParameters(StreamParametersConfig streamParameters) {
        this.streamParameters = streamParameters;
    }

    public String toString() {
        return "DefaultConsumerConfig(streamParameters=" + this.getStreamParameters() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DefaultConsumerConfig)) return false;
        final DefaultConsumerConfig other = (DefaultConsumerConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$streamParameters = this.getStreamParameters();
        final Object other$streamParameters = other.getStreamParameters();
        if (this$streamParameters == null ? other$streamParameters != null : !this$streamParameters.equals(other$streamParameters))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DefaultConsumerConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $streamParameters = this.getStreamParameters();
        result = result * PRIME + ($streamParameters == null ? 43 : $streamParameters.hashCode());
        return result;
    }
}
