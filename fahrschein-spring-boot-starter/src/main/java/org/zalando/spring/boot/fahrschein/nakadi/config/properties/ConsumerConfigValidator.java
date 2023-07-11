package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

class ConsumerConfigValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ConsumerConfig.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "id", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nakadiUrl", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "applicationName", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "consumerGroup", "field.required");
    }
}