package org.zalando.fahrschein.http.apache;

import org.zalando.fahrschein.IdentityAcceptEncodingRequestFactory;
import org.zalando.fahrschein.http.AbstractRequestFactoryTest;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.RequestFactory;

import static org.apache.http.impl.client.HttpClients.*;

public class IdentityEncodingApacheNakadiClientTest extends AbstractRequestFactoryTest {

    @Override
    protected RequestFactory getRequestFactory() {

        return new IdentityAcceptEncodingRequestFactory(new HttpComponentsRequestFactory(createMinimal(), ContentEncoding.IDENTITY));
    }

}
