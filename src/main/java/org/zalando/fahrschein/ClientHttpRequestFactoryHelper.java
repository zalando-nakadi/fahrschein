package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public class ClientHttpRequestFactoryHelper {
    private ClientHttpRequestFactoryHelper() {
    }

    public static ClientHttpRequestFactory wrap(final ClientHttpRequestFactory requestFactory, final ObjectMapper objectMapper, final AccessTokenProvider accessTokenProvider) {
        ClientHttpRequestFactory delegate = requestFactory;

        if (delegate instanceof HttpComponentsClientHttpRequestFactory) {
            delegate = new CloseableHttpComponentsClientRequestFactory((HttpComponentsClientHttpRequestFactory) delegate);
        }

        if (delegate instanceof ProblemHandlingClientHttpRequestFactory) {
            ClientHttpRequestFactory wrapped = ((ProblemHandlingClientHttpRequestFactory) delegate).delegate();
            if (!(wrapped instanceof AuthorizedClientHttpRequestFactory)) {
                delegate = new AuthorizedClientHttpRequestFactory(delegate, accessTokenProvider);
            }
        } else if (delegate instanceof AuthorizedClientHttpRequestFactory) {
            ClientHttpRequestFactory wrapped = ((AuthorizedClientHttpRequestFactory) delegate).delegate();
            if (!(wrapped instanceof ProblemHandlingClientHttpRequestFactory)) {
                delegate = new ProblemHandlingClientHttpRequestFactory(delegate, objectMapper);
            }
        } else {
            delegate = new AuthorizedClientHttpRequestFactory(new ProblemHandlingClientHttpRequestFactory(delegate, objectMapper), accessTokenProvider);
        }

        return delegate;
    }
}
