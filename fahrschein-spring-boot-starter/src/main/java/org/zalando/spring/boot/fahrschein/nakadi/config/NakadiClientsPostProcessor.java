package org.zalando.spring.boot.fahrschein.nakadi.config;

import com.google.common.collect.Lists;
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

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

public class NakadiClientsPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private FahrscheinConfigProperties properties;

    public NakadiClientsPostProcessor() {
    }

    @Override
    public void setEnvironment(Environment environment) {
        final Collection<SettingsParser> parsers = Lists.newArrayList(ServiceLoader.load(SettingsParser.class));
        this.properties = parse((ConfigurableEnvironment) environment, parsers);
        this.properties.postProcess();
    }

    // visible for testing
    FahrscheinConfigProperties parse(final ConfigurableEnvironment environment, final Collection<SettingsParser> parsers) {
        final SettingsParser parser = parsers.stream()
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
