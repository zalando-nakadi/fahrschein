package org.zalando.spring.boot.fahrschein.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.spring.boot.fahrschein.nakadi.config.ObjectMapperFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectMapperFactoryAccessibleTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = ObjectMapperFactory.create();
    }

    @Test
    public void testCreateObjectMapper() {
        assertThat(objectMapper).isNotNull();
    }

}
