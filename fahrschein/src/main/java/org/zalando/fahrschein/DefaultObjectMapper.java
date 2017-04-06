package org.zalando.fahrschein;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

class DefaultObjectMapper {
    static final ObjectMapper INSTANCE;
    static {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(snake_case());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModules(new Jdk8Module(), new ParameterNamesModule(), new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        INSTANCE = objectMapper;
    }

    @SuppressWarnings("deprecation")
    private static PropertyNamingStrategy snake_case() {
        // Use the deprecated constant instead of SNAKE_CASE to remain compatible with jackson versions < 2.7
        return PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;
    }

    private DefaultObjectMapper() {

    }
}
