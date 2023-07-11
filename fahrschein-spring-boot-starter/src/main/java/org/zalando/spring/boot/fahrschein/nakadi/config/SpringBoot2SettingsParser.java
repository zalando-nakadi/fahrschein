package org.zalando.spring.boot.fahrschein.nakadi.config;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ClassUtils;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.FahrscheinConfigProperties;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.SettingsParser;

import static org.springframework.boot.context.properties.source.ConfigurationPropertySources.from;

public class SpringBoot2SettingsParser implements SettingsParser {

    @Override
    public boolean isApplicable() {
        return ClassUtils.isPresent("org.springframework.boot.context.properties.bind.Binder",
                SpringBoot2SettingsParser.class.getClassLoader());
    }

    @Override
    public FahrscheinConfigProperties parse(ConfigurableEnvironment environment) {
        final Iterable<ConfigurationPropertySource> sources = from(environment.getPropertySources());
        PropertySourcesPlaceholdersResolver placeholdersResolver = new PropertySourcesPlaceholdersResolver(environment);
        final Binder binder = new Binder(sources, placeholdersResolver);

        return binder.bindOrCreate("fahrschein", FahrscheinConfigProperties.class);
    }

}
