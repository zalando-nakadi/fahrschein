package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

public class ConsumerConfigValidatorTest {

    @Test
    public void testValidator() {
        ConsumerConfig cc = new ConsumerConfig();
        Errors errors = new BeanPropertyBindingResult(cc, "first");
        ValidationUtils.invokeValidator(new ConsumerConfigValidator(), cc, errors);
        System.out.println("HAS ERRORS : " + errors.hasErrors());
        System.out.println(errors.toString());
    }

}
