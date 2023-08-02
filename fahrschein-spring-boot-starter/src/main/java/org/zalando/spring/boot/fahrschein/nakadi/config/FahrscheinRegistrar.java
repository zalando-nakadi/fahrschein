package org.zalando.spring.boot.fahrschein.nakadi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.spring.boot.fahrschein.config.Registry;
import org.zalando.spring.boot.fahrschein.nakadi.NakadiPublisher;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.AbstractConfig;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.ConsumerConfig;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.DefaultConsumerConfig;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.FahrscheinConfigProperties;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.PublisherConfig;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

public class FahrscheinRegistrar implements NakadiClientsRegistrar {

    private static final String CREATE = "create";

    private static final String LOG_PREFIX = "[{}] - register ";
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(FahrscheinRegistrar.class);

    private final Registry registry;
    private final FahrscheinConfigProperties fahrscheinConfigProperties;

    public FahrscheinRegistrar(Registry registry, FahrscheinConfigProperties fahrscheinConfigProperties) {
        this.registry = registry;
        this.fahrscheinConfigProperties = fahrscheinConfigProperties;
    }

    @Override
    public void register() {
        registerAliasesForNakadiListener();
        registerDefaultObjectMapper();
        fahrscheinConfigProperties.getConsumers().forEach((consumerId, consumerConfig) -> {
            registerNakadiListenerContainer(consumerConfig);
            log.info("[{}] - Consumer registered", consumerConfig.getId());
        });
        registerPublisher(fahrscheinConfigProperties.getPublisher());
    }

    private String registerNakadiListenerContainer(ConsumerConfig consumerConfig) {
        return registry.registerIfAbsent(consumerConfig.getId(), NakadiListenerContainer.class, () -> {
            log.info(LOG_PREFIX + "NakadiListenerContainer ...", consumerConfig.getId());
            return genericBeanDefinition(NakadiListenerContainer.class)
                    .addConstructorArgReference(registerNakadiConsumer(consumerConfig))
                    .addConstructorArgReference(consumerConfig.getId() + "NakadiListener")
                    .addPropertyValue("autoStartup", consumerConfig.getAutostartEnabled());
        });
    }

    private String registerNakadiConsumer(ConsumerConfig consumerConfig) {
        return registry.registerIfAbsent(consumerConfig.getId(), NakadiConsumer.class, () -> {
            log.info(LOG_PREFIX + "NakadiConsumer ...", consumerConfig.getId());
            return genericBeanDefinition(FahrscheinNakadiConsumer.class)
                    .addConstructorArgReference(registerNakadiClient(consumerConfig, "consumer"))
                    .addConstructorArgValue(consumerConfig);
        });
    }

    private String registerPublisher(PublisherConfig publisherConfig) {
        return registry.registerIfAbsent(publisherConfig.getId(), NakadiPublisher.class, () -> {
            log.info(LOG_PREFIX + "NakadiPublisher ...", publisherConfig.getId());
            return genericBeanDefinition(DefaultNakadiPublisher.class)
                    .addConstructorArgReference(registerNakadiClient(publisherConfig, "publisher"));
        });
    }

    private String registerNakadiClient(AbstractConfig consumerConfig, String type) {
        return registry.registerIfAbsent(consumerConfig.getId() + "-" + type, NakadiClient.class, () -> {
            log.info(LOG_PREFIX + "NakadiClient ...", consumerConfig.getId());
            final String requestFactoryRef = registerRequestFactory(consumerConfig);
            return genericBeanDefinition(FahrscheinNakadiClientFactory.class)
                    .addConstructorArgValue(consumerConfig)
                    .addConstructorArgReference(registerCursorManager(consumerConfig, requestFactoryRef))
                    .addConstructorArgReference(registerObjectMapper(consumerConfig))
                    .addConstructorArgReference(requestFactoryRef)
                    .setFactoryMethod(CREATE);
        });
    }

    private String registerRequestFactory(AbstractConfig consumerConfig) {
        return registry.registerIfAbsent(consumerConfig.getId(), RequestFactory.class, () -> {
            log.info(LOG_PREFIX + "RequestFactory ...", consumerConfig.getId());
            return genericBeanDefinition(RequestFactoryFactory.class)
                    .addConstructorArgValue(consumerConfig)
                    .setFactoryMethod(CREATE);
        });
    }

    private String registerObjectMapper(AbstractConfig consumerConfig) {
        // check for objectMapper provided by user via Configuration-Class
        if (registry.isRegistered(consumerConfig.getId(), ObjectMapper.class)) {
            log.info("[{}] - found already registered ObjectMapper: [{}]", consumerConfig.getId(),
                    Registry.generateBeanName(consumerConfig.getId(), ObjectMapper.class));
            return Registry.generateBeanName(consumerConfig.getId(), ObjectMapper.class);
        }
        log.info("[{}] - ObjectMapper used [{}]", consumerConfig.getId(), consumerConfig.getObjectMapperRef());
        return consumerConfig.getObjectMapperRef();
    }

    private String registerCursorManager(AbstractConfig consumerConfig, String requestFactoryRef) {
        return registry.registerIfAbsent("", CursorManager.class, () -> {
            log.info(LOG_PREFIX + "CursorManager ...", consumerConfig.getId());
            return genericBeanDefinition(CursorManagerFactory.class)
                    .addConstructorArgValue(consumerConfig)
                    .addConstructorArgReference(requestFactoryRef)
                    .setFactoryMethod(CREATE);
        });
    }

    private void registerDefaultObjectMapper() {
        if (!registry.isRegistered(DefaultConsumerConfig.FAHRSCHEIN_OBJECT_MAPPER_REF_NAME)) {
            registry.registerIfAbsent("fahrschein", ObjectMapper.class, () -> {
                log.info(LOG_PREFIX + "default ObjectMapper ...",
                        DefaultConsumerConfig.FAHRSCHEIN_OBJECT_MAPPER_REF_NAME);
                return genericBeanDefinition(ObjectMapperFactory.class).setFactoryMethod(CREATE);
            });
        } else {
            log.warn("ObjectMapper '{}' seems to be registered already. Skip registration!",
                    DefaultConsumerConfig.FAHRSCHEIN_OBJECT_MAPPER_REF_NAME);
        }
    }

    private void registerAliasesForNakadiListener() {
        registry.registerAliasesForNakadiListener();
    }
}
