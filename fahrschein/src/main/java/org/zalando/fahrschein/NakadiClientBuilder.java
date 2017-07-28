package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.simple.SimpleRequestFactory;

import javax.annotation.Nullable;
import java.net.URI;

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
    private final RequestFactory clientHttpRequestFactory;
    @Nullable
    private final CursorManager cursorManager;

    NakadiClientBuilder(final URI baseUri) {
        this(baseUri, DefaultObjectMapper.INSTANCE, null, null, null);
    }

    private NakadiClientBuilder(URI baseUri, @Nullable ObjectMapper objectMapper, @Nullable AccessTokenProvider accessTokenProvider, @Nullable RequestFactory clientHttpRequestFactory, @Nullable CursorManager cursorManager) {
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

    public NakadiClientBuilder withRequestFactory(RequestFactory clientHttpRequestFactory) {
        return new NakadiClientBuilder(baseUri, objectMapper, accessTokenProvider, clientHttpRequestFactory, cursorManager);
    }

    public NakadiClientBuilder withCursorManager(CursorManager cursorManager) {
        return new NakadiClientBuilder(baseUri, objectMapper, accessTokenProvider, clientHttpRequestFactory, cursorManager);
    }

    private RequestFactory defaultClientHttpRequestFactory() {
        final SimpleRequestFactory clientHttpRequestFactory = new SimpleRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        clientHttpRequestFactory.setReadTimeout(DEFAULT_READ_TIMEOUT);
        return clientHttpRequestFactory;
    }

    static RequestFactory wrapClientHttpRequestFactory(RequestFactory delegate, @Nullable AccessTokenProvider accessTokenProvider) {
        RequestFactory requestFactory = new ProblemHandlingRequestFactory(delegate);
        if (accessTokenProvider != null) {
            requestFactory = new AuthorizedRequestFactory(requestFactory, accessTokenProvider);
        }

        return requestFactory;
    }

    public NakadiClient build() {
        final RequestFactory clientHttpRequestFactory = wrapClientHttpRequestFactory(this.clientHttpRequestFactory != null ? this.clientHttpRequestFactory : defaultClientHttpRequestFactory(), accessTokenProvider);
        final CursorManager cursorManager = this.cursorManager != null ? this.cursorManager : new ManagedCursorManager(baseUri, clientHttpRequestFactory, true);
        final ObjectMapper objectMapper = this.objectMapper != null ? this.objectMapper : DefaultObjectMapper.INSTANCE;

        return new NakadiClient(baseUri, clientHttpRequestFactory, objectMapper, cursorManager);
    }
}
