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
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        // convert from upper camel to lower camel case.
        String input = type.getSimpleName();
        char firstChar = Character.toLowerCase(input.charAt(0));
        return firstChar + input.substring(1);
    }

    public static <T> String generateBeanName(final String id, final Class<T> type) {
        // convert from lower hyphen to lower camel case.
        List<String> parts = Arrays.asList(id.split("-"));
        return parts.get(0) + parts.subList(1, parts.size()).stream()
                .map(s -> s.isEmpty() ? "" : s.substring(0, 1).toUpperCase(Locale.ENGLISH) + s.substring(1).toLowerCase(Locale.ENGLISH))
                .collect(Collectors.joining()) + type.getSimpleName();
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

    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        if (registry instanceof DefaultListableBeanFactory) {
            DefaultListableBeanFactory factory = (DefaultListableBeanFactory) registry;
            return factory.getBeansOfType(clazz);
        } else {
           LOG.warn("Unable to get beans of type {} from registry of type: {}", clazz.getName(), registry.getClass().getName())
        }
        return Collections.emptyMap();
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
