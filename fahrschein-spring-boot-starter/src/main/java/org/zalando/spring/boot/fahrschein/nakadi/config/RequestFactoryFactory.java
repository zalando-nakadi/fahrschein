package org.zalando.spring.boot.fahrschein.nakadi.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.zalando.fahrschein.http.apache.HttpComponentsRequestFactory;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.AbstractConfig;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.HttpConfig;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class RequestFactoryFactory {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(RequestFactoryFactory.class);

    static RequestFactory create(AbstractConfig config) {
        CloseableHttpClient closeableHttpClient = buildCloseableHttpClient(config.getHttp());
        log.info("HttpClient configured for [{}] with http-config : {}", config.getId(), config.getHttp().toString());
        return new HttpComponentsRequestFactory(closeableHttpClient, config.getHttp().getContentEncoding());
    }

    protected static CloseableHttpClient buildCloseableHttpClient(HttpConfig httpConfig) {
        final RequestConfig config = RequestConfig.custom()
                .setSocketTimeout((int) MILLISECONDS.convert(httpConfig.getSocketTimeout().getAmount(), httpConfig.getSocketTimeout().getUnit()))
                .setConnectTimeout((int) MILLISECONDS.convert(httpConfig.getConnectTimeout().getAmount(), httpConfig.getConnectTimeout().getUnit()))
                .setConnectionRequestTimeout((int) MILLISECONDS.convert(httpConfig.getConnectionRequestTimeout().getAmount(), httpConfig.getConnectionRequestTimeout().getUnit()))
                .build();

        final ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setBufferSize(httpConfig.getBufferSize())
                .build();

        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setDefaultConnectionConfig(connectionConfig)
                .setConnectionTimeToLive(httpConfig.getConnectionTimeToLive().getAmount(), httpConfig.getConnectionTimeToLive().getUnit())
                .disableRedirectHandling()
                .setMaxConnTotal(httpConfig.getMaxConnectionsTotal())
                .setMaxConnPerRoute(httpConfig.getMaxConnectionsPerRoute())
                .setUserAgent(httpConfig.getUserAgent());

        if (httpConfig.getEvictExpiredConnections()) {
            builder = builder.evictExpiredConnections();
        }

        if (httpConfig.getEvictIdleConnections()) {
            builder = builder.evictIdleConnections(httpConfig.getMaxIdleTime().longValue(), MILLISECONDS);
        }


        return builder.build();
    }
}
