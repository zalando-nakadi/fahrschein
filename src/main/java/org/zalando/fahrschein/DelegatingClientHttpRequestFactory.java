package org.zalando.fahrschein;

import org.springframework.http.client.ClientHttpRequestFactory;

public interface DelegatingClientHttpRequestFactory extends ClientHttpRequestFactory {
    ClientHttpRequestFactory delegate();
}
