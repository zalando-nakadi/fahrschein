package org.zalando.fahrschein.http.spring;

import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.http.AbstractRequestFactoryTest;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.util.concurrent.TimeUnit;

public class SpringNakadiClientTest extends AbstractRequestFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger("okhttp3.wire");
    private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(logger::debug);
    static {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
    }

    @Override
    protected RequestFactory getRequestFactory() {
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(2, 5*60, TimeUnit.SECONDS))
                .build();

        final OkHttp3ClientHttpRequestFactory clientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory(client);
        return new SpringRequestFactory(clientHttpRequestFactory);
    }

}
