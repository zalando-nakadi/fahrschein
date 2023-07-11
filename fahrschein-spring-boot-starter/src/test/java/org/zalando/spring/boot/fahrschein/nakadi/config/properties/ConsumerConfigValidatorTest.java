package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsumerConfigValidatorTest {

    @Test
    public void testValidator() {
        ConsumerConfig cc = new ConsumerConfig();
        Errors errors = new BeanPropertyBindingResult(cc, "first");
        ValidationUtils.invokeValidator(new ConsumerConfigValidator(), cc, errors);

        final var expectedErrors = new String[]{
                "Field error in object 'first' on field 'id': rejected value [null]; codes [field.required.first.id,field.required.id,field.required.java.lang.String,field.required]; arguments []; default message [null]",
                "Field error in object 'first' on field 'nakadiUrl': rejected value [null]; codes [field.required.first.nakadiUrl,field.required.nakadiUrl,field.required.java.lang.String,field.required]; arguments []; default message [null]",
                "Field error in object 'first' on field 'applicationName': rejected value [null]; codes [field.required.first.applicationName,field.required.applicationName,field.required.java.lang.String,field.required]; arguments []; default message [null]",
                "Field error in object 'first' on field 'consumerGroup': rejected value [null]; codes [field.required.first.consumerGroup,field.required.consumerGroup,field.required.java.lang.String,field.required]; arguments []; default message [null]"
        };
        assertThat(errors.getAllErrors().stream().map(ObjectError::toString).toList().toArray())
                .isEqualTo(expectedErrors);
    }

}
