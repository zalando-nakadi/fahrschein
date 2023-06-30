package org.zalando.spring.boot.fahrschein.test;

import org.junit.jupiter.api.Test;
import org.zalando.spring.boot.fahrschein.nakadi.config.ObjectMapperFactory;

public class ObjectMapperFactoryAccessibleTest {

    @Test
    public void testCreateObjectMapper() {
        ObjectMapperFactory.create();
    }

}
