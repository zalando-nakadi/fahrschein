package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@EqualsAndHashCode(callSuper = false)
public class DefaultConsumerConfig extends AbstractConfig {

    public static final String FAHRSCHEIN_OBJECT_MAPPER_REF_NAME = "fahrscheinObjectMapper";
    public static final String SUBSCRIPTION_BY_ID_DEFAULT = "subscription-by-id-default";
    private StreamParametersConfig streamParameters = new StreamParametersConfig();

    public DefaultConsumerConfig() {
        super();
        setHttp(HttpConfig.defaultHttpConfig());
        setId("CONFIG_DEFAULT");
        setAutostartEnabled(Boolean.TRUE);
        setReadFrom(Position.END);
        setRecordMetrics(Boolean.FALSE);
        setObjectMapperRef(FAHRSCHEIN_OBJECT_MAPPER_REF_NAME);
        setSubscriptionById(SUBSCRIPTION_BY_ID_DEFAULT);
    }

}
