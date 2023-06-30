package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "fahrschein")
public class FahrscheinConfigProperties {

    private static final String CREDENTIALS_DIRECTORY_PATH = "/meta/credentials";

    @NestedConfigurationProperty
    private DefaultConsumerConfig defaults = new DefaultConsumerConfig();

    private Map<String, ConsumerConfig> consumers = new LinkedHashMap<>();

    private PublisherConfig publisher = new PublisherConfig();

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
}
