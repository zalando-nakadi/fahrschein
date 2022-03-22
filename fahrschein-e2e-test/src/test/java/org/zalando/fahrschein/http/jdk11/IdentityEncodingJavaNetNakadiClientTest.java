package org.zalando.fahrschein.http.jdk11;

import org.zalando.fahrschein.IdentityAcceptEncodingRequestFactory;
import org.zalando.fahrschein.http.AbstractRequestFactoryTest;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.net.http.HttpClient;
import java.time.Duration;

public class IdentityEncodingJavaNetNakadiClientTest extends AbstractRequestFactoryTest {
    @Override
    protected RequestFactory getRequestFactory() {
        return new IdentityAcceptEncodingRequestFactory(new JavaNetRequestFactory(HttpClient.newBuilder().build(), rb -> rb.timeout(Duration.ofSeconds(1)), ContentEncoding.IDENTITY));
    }
}
