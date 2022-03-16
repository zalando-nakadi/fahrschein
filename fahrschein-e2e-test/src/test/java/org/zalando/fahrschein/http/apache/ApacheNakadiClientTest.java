package org.zalando.fahrschein.http.apache;

import org.zalando.fahrschein.http.AbstractRequestFactoryTest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.util.concurrent.TimeUnit;

public class ApacheNakadiClientTest extends AbstractRequestFactoryTest {

    @Override
    protected RequestFactory getRequestFactory() {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(60000)
                .setConnectTimeout(2000)
                .setConnectionRequestTimeout(8000)
                .setContentCompressionEnabled(false)
                .build();

        final ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setBufferSize(512)
                .build();

        final CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultConnectionConfig(connectionConfig)
                .setConnectionTimeToLive(30, TimeUnit.SECONDS)
                .disableAutomaticRetries()
                .disableRedirectHandling()
                .setMaxConnTotal(8)
                .setMaxConnPerRoute(2)
                .build();

        return new HttpComponentsRequestFactory(httpClient);
    }

}
