package org.zalando.spring.boot.fahrschein.nakadi;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.spring.boot.fahrschein.config.Registry;

public class RegistryTest {

    @Test
    public void testgeneratedBeanName() {
        String beanName = Registry.generateBeanName("first", NakadiClient.class);
        Assertions.assertThat(beanName).isEqualTo("firstNakadiClient");
    }

}
