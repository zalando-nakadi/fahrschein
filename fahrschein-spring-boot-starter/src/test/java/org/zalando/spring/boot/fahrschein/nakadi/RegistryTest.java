package org.zalando.spring.boot.fahrschein.nakadi;

import org.junit.jupiter.api.Test;
import org.zalando.fahrschein.NakadiClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.spring.boot.fahrschein.config.Registry.generateBeanName;

public class RegistryTest {

    @Test
    public void testgeneratedBeanNameSimple() {
        assertThat(generateBeanName(NakadiClient.class)).isEqualTo("nakadiClient");
        assertThat(generateBeanName("", NakadiClient.class)).isEqualTo("NakadiClient");
        assertThat(generateBeanName("first", NakadiClient.class)).isEqualTo("firstNakadiClient");
        assertThat(generateBeanName("first-little", NakadiClient.class)).isEqualTo("firstLittleNakadiClient");
    }

}
