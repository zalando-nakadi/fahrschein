package org.zalando.fahrschein.http.jdk11;

import org.zalando.fahrschein.http.AbstractRequestFactoryTest;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

public class JavaNetNakadiClientTest extends AbstractRequestFactoryTest {
    @Override
    protected RequestFactory getRequestFactory() {
        return new JavaNetRequestFactory(HttpClient.newBuilder().build(), Optional.of(Duration.ofSeconds(1)), ContentEncoding.GZIP);
    }
}
