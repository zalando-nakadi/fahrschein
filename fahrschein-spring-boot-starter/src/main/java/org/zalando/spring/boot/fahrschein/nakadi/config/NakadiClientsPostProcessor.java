package org.zalando.spring.boot.fahrschein.nakadi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.zalando.spring.boot.fahrschein.config.Registry;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.FahrscheinConfigProperties;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.SettingsParser;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public class NakadiClientsPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private FahrscheinConfigProperties properties;

    @Override
    public void setEnvironment(Environment environment) {
        final Iterable<SettingsParser> parsers = ServiceLoader.load(SettingsParser.class);
        this.properties = parse((ConfigurableEnvironment) environment, parsers);
        this.properties.postProcess();
    }

    // visible for testing
    FahrscheinConfigProperties parse(final ConfigurableEnvironment environment, final Iterable<SettingsParser> parsers) {
        final SettingsParser parser = StreamSupport.stream(parsers.spliterator(), false)
                .filter(SettingsParser::isApplicable)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No applicable nakadi-clients settings parser available"));

        return parser.parse(environment);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        final NakadiClientsRegistrar registrar = new FahrscheinRegistrar(new Registry(registry), properties);
        registrar.register();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerSingleton("fahrscheinConfigProperties", properties);
    }
}
