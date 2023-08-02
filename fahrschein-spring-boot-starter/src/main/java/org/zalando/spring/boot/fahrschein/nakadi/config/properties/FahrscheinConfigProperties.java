package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.Map;

@Validated
@Component
@ConfigurationProperties(prefix = "fahrschein")
public class FahrscheinConfigProperties {

    private static final String CREDENTIALS_DIRECTORY_PATH = "/meta/credentials";
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(FahrscheinConfigProperties.class);

    @NestedConfigurationProperty
    private DefaultConsumerConfig defaults = new DefaultConsumerConfig();

    private Map<String, ConsumerConfig> consumers = new LinkedHashMap<>();

    private PublisherConfig publisher = new PublisherConfig();

    public FahrscheinConfigProperties() {
    }

    public void postProcess() {
        defaults.getOauth().setCredentialsDirectoryIfNotConfigured(CREDENTIALS_DIRECTORY_PATH);
        consumers.forEach((key, value) -> {
            value.mergeWithDefaultConfig(defaults);
            value.setId(key);
            value.getOauth().setAccessTokenIdIfNotConfigured(key);
            final Errors errors = new BeanPropertyBindingResult(value, key);
            ValidationUtils.invokeValidator(new ConsumerConfigValidator(), value, errors);
            if (errors.hasErrors()) {
                log.warn(errors.toString());
                log.warn("Will throw an exception in future");
            }
        });
        publisher.mergeWithDefaultConfig(defaults);
    }

    public DefaultConsumerConfig getDefaults() {
        return this.defaults;
    }

    public Map<String, ConsumerConfig> getConsumers() {
        return this.consumers;
    }

    public PublisherConfig getPublisher() {
        return this.publisher;
    }

    public void setDefaults(DefaultConsumerConfig defaults) {
        this.defaults = defaults;
    }

    public void setConsumers(Map<String, ConsumerConfig> consumers) {
        this.consumers = consumers;
    }

    public void setPublisher(PublisherConfig publisher) {
        this.publisher = publisher;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof FahrscheinConfigProperties)) return false;
        final FahrscheinConfigProperties other = (FahrscheinConfigProperties) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$defaults = this.getDefaults();
        final Object other$defaults = other.getDefaults();
        if (this$defaults == null ? other$defaults != null : !this$defaults.equals(other$defaults)) return false;
        final Object this$consumers = this.getConsumers();
        final Object other$consumers = other.getConsumers();
        if (this$consumers == null ? other$consumers != null : !this$consumers.equals(other$consumers)) return false;
        final Object this$publisher = this.getPublisher();
        final Object other$publisher = other.getPublisher();
        if (this$publisher == null ? other$publisher != null : !this$publisher.equals(other$publisher)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof FahrscheinConfigProperties;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $defaults = this.getDefaults();
        result = result * PRIME + ($defaults == null ? 43 : $defaults.hashCode());
        final Object $consumers = this.getConsumers();
        result = result * PRIME + ($consumers == null ? 43 : $consumers.hashCode());
        final Object $publisher = this.getPublisher();
        result = result * PRIME + ($publisher == null ? 43 : $publisher.hashCode());
        return result;
    }

    public String toString() {
        return "FahrscheinConfigProperties(defaults=" + this.getDefaults() + ", consumers=" + this.getConsumers() + ", publisher=" + this.getPublisher() + ")";
    }
}
