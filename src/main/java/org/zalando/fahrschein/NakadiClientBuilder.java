package org.zalando.fahrschein;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.ofNullable;

public final class NakadiClientBuilder {
    public static final int DEFAULT_CONNECT_TIMEOUT = 500;
    public static final int DEFAULT_READ_TIMEOUT = 60 * 1000;

    private final URI baseUri;
    private AccessTokenProvider accessTokenProvider;
    private ClientHttpRequestFactory clientHttpRequestFactory;
    private CursorManager cursorManager;

    NakadiClientBuilder(final URI baseUri) {
        this.baseUri = checkNotNull(baseUri, "Base URI should not be null");
    }

    private NakadiClientBuilder(URI baseUri, AccessTokenProvider accessTokenProvider, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager) {
        this.baseUri = baseUri;
        this.accessTokenProvider = accessTokenProvider;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.cursorManager = cursorManager;
    }

    public NakadiClientBuilder withAccessTokenProvider(AccessTokenProvider accessTokenProvider) {
        return new NakadiClientBuilder(baseUri, accessTokenProvider, clientHttpRequestFactory, cursorManager);
    }

    public NakadiClientBuilder withClientHttpRequestFactory(ClientHttpRequestFactory clientHttpRequestFactory) {
        return new NakadiClientBuilder(baseUri, accessTokenProvider, clientHttpRequestFactory, cursorManager);
    }

    public NakadiClientBuilder withCursorManager(CursorManager cursorManager) {
        return new NakadiClientBuilder(baseUri, accessTokenProvider, clientHttpRequestFactory, cursorManager);
    }

    private ClientHttpRequestFactory defaultClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory requestFactoryDelegate = new SimpleClientHttpRequestFactory();
        requestFactoryDelegate.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        requestFactoryDelegate.setReadTimeout(DEFAULT_READ_TIMEOUT);

        ClientHttpRequestFactory requestFactory = new ProblemHandlingClientHttpRequestFactory(requestFactoryDelegate);
        if (accessTokenProvider != null) {
            requestFactory = new AuthorizedClientHttpRequestFactory(requestFactory, accessTokenProvider);
        }

        return requestFactory;
    }

    public NakadiClient build() {
        final ClientHttpRequestFactory clientHttpRequestFactory = ofNullable(this.clientHttpRequestFactory).orElseGet(this::defaultClientHttpRequestFactory);
        final CursorManager cursorManager = ofNullable(this.cursorManager).orElseGet(() -> new ManagedCursorManager(baseUri, clientHttpRequestFactory));

        return new NakadiClient(baseUri, clientHttpRequestFactory, cursorManager);
    }
}
