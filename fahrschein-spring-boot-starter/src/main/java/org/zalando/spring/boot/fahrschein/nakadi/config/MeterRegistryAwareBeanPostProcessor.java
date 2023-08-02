package org.zalando.spring.boot.fahrschein.nakadi.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.zalando.spring.boot.fahrschein.nakadi.MeterRegistryAware;

public class MeterRegistryAwareBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MeterRegistryAwareBeanPostProcessor.class);
    private BeanFactory beanFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof MeterRegistryAware) {
            final MeterRegistry reg = beanFactory.getBean(MeterRegistry.class);
            ((MeterRegistryAware) bean).setMeterRegistry(reg);
            log.debug("MeterRegistry injected into bean : {}", beanName);
        }
        return bean;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
