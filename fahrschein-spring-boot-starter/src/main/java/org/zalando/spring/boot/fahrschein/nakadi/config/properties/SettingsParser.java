package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.springframework.core.env.ConfigurableEnvironment;

public interface SettingsParser {

    boolean isApplicable();

    FahrscheinConfigProperties parse(ConfigurableEnvironment environment);

}
