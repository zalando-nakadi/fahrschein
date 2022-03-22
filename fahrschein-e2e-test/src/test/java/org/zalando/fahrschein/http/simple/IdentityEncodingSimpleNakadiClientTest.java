package org.zalando.fahrschein.http.simple;

import org.zalando.fahrschein.IdentityAcceptEncodingRequestFactory;
import org.zalando.fahrschein.http.AbstractRequestFactoryTest;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.RequestFactory;

public class IdentityEncodingSimpleNakadiClientTest extends AbstractRequestFactoryTest {
    @Override
    protected RequestFactory getRequestFactory() {
        return new IdentityAcceptEncodingRequestFactory(new SimpleRequestFactory(ContentEncoding.IDENTITY));
    }
}
