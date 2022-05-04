package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.fahrschein.http.api.RequestFactory;
import javax.annotation.Nullable;
import java.net.URI;

import static org.zalando.fahrschein.Preconditions.checkNotNull;

public final class NakadiClientBuilder {

    private final URI baseUri;
    @Nullable
    private final ObjectMapper objectMapper;
    @Nullable
    private final AuthorizationProvider authorizationProvider;
    @Nullable
    private final RequestFactory clientHttpRequestFactory;
    @Nullable
    private final CursorManager cursorManager;

    NakadiClientBuilder(final URI baseUri, RequestFactory requestFactory) {
        this(baseUri, DefaultObjectMapper.INSTANCE, null, requestFactory, null);
    }

    private NakadiClientBuilder(URI baseUri, @Nullable ObjectMapper objectMapper, @Nullable AuthorizationProvider authorizationProvider, @Nullable RequestFactory clientHttpRequestFactory, @Nullable CursorManager cursorManager) {
        this.objectMapper = objectMapper;
        this.baseUri = checkNotNull(baseUri, "Base URI should not be null");
        this.authorizationProvider = authorizationProvider;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.cursorManager = cursorManager;
    }

    public NakadiClientBuilder withObjectMapper(ObjectMapper objectMapper) {
        return new NakadiClientBuilder(baseUri, objectMapper, authorizationProvider, clientHttpRequestFactory, cursorManager);
    }

    public NakadiClientBuilder withAccessTokenProvider(AccessTokenProvider accessTokenProvider) {
        return withAuthorizationProvider(accessTokenProvider);
    }

    public NakadiClientBuilder withAuthorizationProvider(AuthorizationProvider authorizationProvider) {
        return new NakadiClientBuilder(baseUri, objectMapper, authorizationProvider, clientHttpRequestFactory, cursorManager);
    }

    public NakadiClientBuilder withCursorManager(CursorManager cursorManager) {
        return new NakadiClientBuilder(baseUri, objectMapper, authorizationProvider, clientHttpRequestFactory, cursorManager);
    }

    static RequestFactory wrapClientHttpRequestFactory(RequestFactory delegate, @Nullable AuthorizationProvider authorizationProvider) {
        RequestFactory requestFactory = new ProblemHandlingRequestFactory(delegate);
        if (authorizationProvider != null) {
            requestFactory = new AuthorizedRequestFactory(requestFactory, authorizationProvider);
        }

        return requestFactory;
    }

    /**
     * Creates a new instance of {@code NakadiClient}. In case no {@code ObjectMapper} is provided, it's going to make
     * use of {@code DefaultObjectMapper} that is making use of
     * {@code PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES}.
     * In case no {@code CursorManager} is provided it's going to make use of {@code ManagedCursorManager}.
     *
     * @return A fresh instance of {@code NakadiClient}
     */
    public NakadiClient build() {
        final RequestFactory clientHttpRequestFactory = wrapClientHttpRequestFactory(this.clientHttpRequestFactory, authorizationProvider);
        final CursorManager cursorManager = this.cursorManager != null ? this.cursorManager : new ManagedCursorManager(baseUri, clientHttpRequestFactory, true);
        final ObjectMapper objectMapper = this.objectMapper != null ? this.objectMapper : DefaultObjectMapper.INSTANCE;

        return new NakadiClient(baseUri, clientHttpRequestFactory, objectMapper, cursorManager);
    }
}
