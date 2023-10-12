package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public abstract class AbstractConfig {

    private String id;

    private Boolean autostartEnabled;

    private String nakadiUrl;

    private String applicationName;

    private String consumerGroup;

    private Position readFrom;

    private Boolean recordMetrics;

    private String objectMapperRef;

    @NestedConfigurationProperty
    protected OAuthConfig oauth = OAuthConfig.defaultOAuthConfig();

    @NestedConfigurationProperty
    protected HttpConfig http = new HttpConfig();

    @NestedConfigurationProperty
    protected AuthorizationsConfig authorizations = new AuthorizationsConfig();

    @NestedConfigurationProperty
    protected BackoffConfig backoff = BackoffConfig.defaultBackoffConfig();

    protected ThreadConfig threads = new ThreadConfig();

    @NestedConfigurationProperty
    protected SubscriptionConfig subscription = SubscriptionConfig.defaultSubscriptionConfig();

}
