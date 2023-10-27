package org.zalando.spring.boot.fahrschein.nakadi.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.util.ClassUtils;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.jdk11.JavaNetRequestFactory;
import org.zalando.fahrschein.http.spring.SpringRequestFactory;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.HttpConfig;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class RequestFactories {

    private static final String JDK11_REQUEST_FACTORY_CLASS = "org.zalando.fahrschein.http.jdk11.JavaNetRequestFactory";
    private static final boolean JDK11_REQUEST_FACTORY_IS_PRESENT = ClassUtils.isPresent(JDK11_REQUEST_FACTORY_CLASS, null);

    /**
     * prefer JDK11 request factory before delegating to the spring request factory
     */
    public static RequestFactory create(HttpConfig config) {
        if (JDK11_REQUEST_FACTORY_IS_PRESENT) {
            return new JavaNetRequestFactory(HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis((int) MILLISECONDS.convert(config.getConnectTimeout().getAmount(), config.getConnectTimeout().getUnit()))).build(),
                    Optional.of(Duration.ofMillis(
                    (int) MILLISECONDS.convert(config.getRequestTimeout().getAmount(), config.getRequestTimeout().getUnit())
            )), config.getContentEncoding());
        }
        ClientHttpRequestFactorySettings settings = httpConfigToClientHttpRequestFactorySettings(config);
        return new SpringRequestFactory(ClientHttpRequestFactories.get(settings), config.getContentEncoding());
    }

    private static ClientHttpRequestFactorySettings httpConfigToClientHttpRequestFactorySettings(HttpConfig httpConfig) {
        return new ClientHttpRequestFactorySettings(
                Duration.ofMillis((int) MILLISECONDS.convert(httpConfig.getConnectTimeout().getAmount(), httpConfig.getConnectTimeout().getUnit())),
                Duration.ofMillis((int) MILLISECONDS.convert(httpConfig.getSocketTimeout().getAmount(), httpConfig.getSocketTimeout().getUnit())), true);
    }

}
