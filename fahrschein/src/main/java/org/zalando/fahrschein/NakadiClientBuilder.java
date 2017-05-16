package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.http.simple.SimpleClientHttpRequestFactory;

import javax.annotation.Nullable;
import java.net.URI;

import static java.util.Optional.ofNullable;
import static org.zalando.fahrschein.Preconditions.checkNotNull;

public final class NakadiClientBuilder {
    public static final int DEFAULT_CONNECT_TIMEOUT = 500;
    public static final int DEFAULT_READ_TIMEOUT = 60 * 1000;

    private final URI baseUri;
    @Nullable
    private final ObjectMapper objectMapper;
    @Nullable
    private final AccessTokenProvider accessTokenProvider;
    @Nullable
    private final ClientHttpRequestFactory clientHttpRequestFactory;
    @Nullable
    private final CursorManager cursorManager;

    NakadiClientBuilder(final URI baseUri) {
        this(baseUri, DefaultObjectMapper.INSTANCE, null, null, null);
    }

    private NakadiClientBuilder(URI baseUri, @Nullable ObjectMapper objectMapper, @Nullable AccessTokenProvider accessTokenProvider, @Nullable ClientHttpRequestFactory clientHttpRequestFactory, @Nullable CursorManager cursorManager) {
        this.objectMapper = objectMapper;
        this.baseUri = checkNotNull(baseUri, "Base URI should not be null");
        this.accessTokenProvider = accessTokenProvider;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.cursorManager = cursorManager;
    }

    public NakadiClientBuilder withObjectMapper(ObjectMapper objectMapper) {
        return new NakadiClientBuilder(baseUri, objectMapper, accessTokenProvider, clientHttpRequestFactory, cursorManager);
    }

    public NakadiClientBuilder withAccessTokenProvider(AccessTokenProvider accessTokenProvider) {
        return new NakadiClientBuilder(baseUri, objectMapper, accessTokenProvider, clientHttpRequestFactory, cursorManager);
    }

    public NakadiClientBuilder withClientHttpRequestFactory(ClientHttpRequestFactory clientHttpRequestFactory) {
        return new NakadiClientBuilder(baseUri, objectMapper, accessTokenProvider, clientHttpRequestFactory, cursorManager);
    }

    public NakadiClientBuilder withCursorManager(CursorManager cursorManager) {
        return new NakadiClientBuilder(baseUri, objectMapper, accessTokenProvider, clientHttpRequestFactory, cursorManager);
    }

    private ClientHttpRequestFactory defaultClientHttpRequestFactory() {
        final SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        clientHttpRequestFactory.setReadTimeout(DEFAULT_READ_TIMEOUT);
        return clientHttpRequestFactory;
    }

    private ClientHttpRequestFactory wrapClientHttpRequestFactory(ClientHttpRequestFactory delegate) {
        ClientHttpRequestFactory requestFactory = new ProblemHandlingClientHttpRequestFactory(delegate);
        if (accessTokenProvider != null) {
            requestFactory = new AuthorizedClientHttpRequestFactory(requestFactory, accessTokenProvider);
        }

        return requestFactory;
    }

    public NakadiClient build() {
        final ClientHttpRequestFactory clientHttpRequestFactory = wrapClientHttpRequestFactory(ofNullable(this.clientHttpRequestFactory).orElseGet(this::defaultClientHttpRequestFactory));
        final CursorManager cursorManager = ofNullable(this.cursorManager).orElseGet(() -> new ManagedCursorManager(baseUri, clientHttpRequestFactory));
        final ObjectMapper objectMapper = this.objectMapper != null ? this.objectMapper : DefaultObjectMapper.INSTANCE;

        return new NakadiClient(baseUri, clientHttpRequestFactory, objectMapper, cursorManager);
    }
}
