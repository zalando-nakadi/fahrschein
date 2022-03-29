package org.zalando.fahrschein.http.apache;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.zalando.fahrschein.http.AbstractRequestFactoryTest;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.RequestFactory;

public class ApacheNakadiClientTest extends AbstractRequestFactoryTest {

    @Override
    protected RequestFactory getRequestFactory() {
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        return new HttpComponentsRequestFactory(httpClient, ContentEncoding.GZIP);
    }

}
