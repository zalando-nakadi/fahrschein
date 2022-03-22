package org.zalando.fahrschein.http.simple;

import org.zalando.fahrschein.http.AbstractRequestFactoryTest;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.RequestFactory;

public class SimpleNakadiClientTest extends AbstractRequestFactoryTest {
    @Override
    protected RequestFactory getRequestFactory() {
        return new SimpleRequestFactory(ContentEncoding.GZIP);
    }
}
