package org.zalando.spring.boot.fahrschein.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.zalando.spring.boot.fahrschein.nakadi.stereotype.NakadiEventListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

public class Registry {

    private static final Logger LOG = LoggerFactory.getLogger(Registry.class);

    private final BeanDefinitionRegistry registry;

    public Registry(BeanDefinitionRegistry beanDefinitionRegistry) {
        this.registry = beanDefinitionRegistry;
    }


    public boolean isRegistered(final Class<?> type) {
        return isRegistered(generateBeanName(type));
    }

    public boolean isRegistered(final String id, final Class<?> type) {
        return isRegistered(generateBeanName(id, type));
    }

    public boolean isRegistered(final String name) {
        return registry.isBeanNameInUse(name);
    }

    public <T> String registerIfAbsent(final Class<T> type, final Supplier<BeanDefinitionBuilder> factory) {
        final String name = generateBeanName(type);

        if (isRegistered(name)) {
            LOG.debug("Bean [{}] is already registered, skipping it.", name);
            return name;
        }

        registry.registerBeanDefinition(name, factory.get().getBeanDefinition());

        return name;
    }

    public <T> String registerIfAbsent(final String id, final Class<T> type) {
        return registerIfAbsent(id, type, () -> genericBeanDefinition(type));
    }

    public <T> String registerIfAbsent(final String id, final Class<T> type,
            final Supplier<BeanDefinitionBuilder> factory) {

        final String name = generateBeanName(id, type);

        if (isRegistered(name)) {
            LOG.debug("Bean [{}] is already registered, skipping it.", name);
            return name;
        }

        final AbstractBeanDefinition definition = factory.get().getBeanDefinition();

        definition.addQualifier(new AutowireCandidateQualifier(Qualifier.class, id));
        // seems it is not possible to have multiple AutowireCandidateQualifier for same 'type'
        // with different 'value'
//        definition.addQualifier(new AutowireCandidateQualifier(Qualifier.class, name));

        registry.registerBeanDefinition(name, definition);

        return name;
    }

    public static <T> String generateBeanName(final Class<T> type) {
        return UPPER_CAMEL.to(LOWER_CAMEL, type.getSimpleName());
    }

    public static <T> String generateBeanName(final String id, final Class<T> type) {
        return LOWER_HYPHEN.to(LOWER_CAMEL, id) + type.getSimpleName();
    }

    public static BeanReference ref(final String beanName) {
        return new RuntimeBeanReference(beanName);
    }

    @SafeVarargs
    public static <T> List<T> list(final T... elements) {
        final ManagedList<T> list = new ManagedList<>();
        Collections.addAll(list, elements);
        return list;
    }


	public void registerAliasesForNakadiListener() {
		if (registry instanceof DefaultListableBeanFactory) {
			DefaultListableBeanFactory factory = (DefaultListableBeanFactory) registry;
			String[] beans = factory.getBeanNamesForAnnotation(NakadiEventListener.class);
			Arrays.asList(beans).forEach(bean -> {
				String alias = bean + "NakadiListener";
				registry.registerAlias(bean, alias);
				LOG.info("Register alias [{}] for bean [{}]", alias, bean);
			});
		} else {
			LOG.warn("Unable to register aliases for @NakadiEventListener annotated components. Registry of type : {}", registry.getClass().getName());
		}

	}
}
