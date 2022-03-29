package org.zalando.fahrschein.http.spring;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.zalando.fahrschein.IdentityAcceptEncodingRequestFactory;
import org.zalando.fahrschein.http.AbstractRequestFactoryTest;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.util.concurrent.TimeUnit;

public class IdentityEncodingSpringNakadiClientTest extends AbstractRequestFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger("okhttp3.wire");
    private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(logger::debug);
    static {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
    }

    @Override
    protected RequestFactory getRequestFactory() {
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        final OkHttp3ClientHttpRequestFactory clientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory(client);
        return new IdentityAcceptEncodingRequestFactory(new SpringRequestFactory(clientHttpRequestFactory, ContentEncoding.IDENTITY));
    }

}
