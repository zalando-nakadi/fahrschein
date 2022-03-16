package org.zalando.fahrschein.http.spring;

import org.zalando.fahrschein.http.AbstractRequestFactoryTest;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.util.concurrent.TimeUnit;

public class SpringNakadiClientTest extends AbstractRequestFactoryTest {

    @Override
    protected RequestFactory getRequestFactory() {
        final OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(2, 5*60, TimeUnit.SECONDS))
                .build();

        final OkHttp3ClientHttpRequestFactory clientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory(client);
        return new SpringRequestFactory(clientHttpRequestFactory);
    }

}
