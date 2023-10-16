package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.fahrschein.http.api.RequestFactory;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    @Nullable
    private final List<EventPublishingHandler> eventPublishingHandlers;

    private final BackoffStrategy backoffStrategy;

    NakadiClientBuilder(final URI baseUri, RequestFactory requestFactory) {
        this(baseUri, DefaultObjectMapper.INSTANCE, null, requestFactory, null, Collections.emptyList(),null);
    }

    private NakadiClientBuilder(URI baseUri, @Nullable ObjectMapper objectMapper, @Nullable AuthorizationProvider authorizationProvider,
                                @Nullable RequestFactory clientHttpRequestFactory, @Nullable CursorManager cursorManager, final BackoffStrategy backoffStrategy) {
        this.objectMapper = objectMapper;
        this.baseUri = checkNotNull(baseUri, "Base URI should not be null");
        this.authorizationProvider = authorizationProvider;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.cursorManager = cursorManager;
        this.eventPublishingHandlers = new ArrayList<>();
        this.backoffStrategy = backoffStrategy;
    }

    public NakadiClientBuilder(URI baseUri, @Nullable ObjectMapper objectMapper, @Nullable AuthorizationProvider authorizationProvider,
                               @Nullable RequestFactory clientHttpRequestFactory, @Nullable CursorManager cursorManager,
                               @Nullable List<EventPublishingHandler> eventPublishingHandlers, @Nullable final BackoffStrategy backoffStrategy) {
        this.objectMapper = objectMapper;
        this.baseUri = checkNotNull(baseUri, "Base URI should not be null");
        this.authorizationProvider = authorizationProvider;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.cursorManager = cursorManager;
        this.eventPublishingHandlers = eventPublishingHandlers;
        this.backoffStrategy = backoffStrategy;
    }


    public NakadiClientBuilder withObjectMapper(ObjectMapper objectMapper) {
        return new NakadiClientBuilder(baseUri, objectMapper, authorizationProvider, clientHttpRequestFactory, cursorManager, backoffStrategy);
    }

    public NakadiClientBuilder withAccessTokenProvider(AccessTokenProvider accessTokenProvider) {
        return withAuthorizationProvider(accessTokenProvider);
    }

    public NakadiClientBuilder withAuthorizationProvider(AuthorizationProvider authorizationProvider) {
        return new NakadiClientBuilder(baseUri, objectMapper, authorizationProvider, clientHttpRequestFactory, cursorManager, backoffStrategy);
    }

    public NakadiClientBuilder withCursorManager(CursorManager cursorManager) {
        return new NakadiClientBuilder(baseUri, objectMapper, authorizationProvider, clientHttpRequestFactory, cursorManager, backoffStrategy);
    }

    public NakadiClientBuilder withRequestHandlers(List<EventPublishingHandler> eventPublishingHandlers) {
        return new NakadiClientBuilder(baseUri, objectMapper, authorizationProvider, clientHttpRequestFactory, cursorManager, eventPublishingHandlers,backoffStrategy);
    }

    public NakadiClientBuilder withRequestHandler(EventPublishingHandler eventPublishingHandler) {
        eventPublishingHandlers.add(eventPublishingHandler);
        return new NakadiClientBuilder(baseUri, objectMapper, authorizationProvider, clientHttpRequestFactory, cursorManager, eventPublishingHandlers,backoffStrategy);
    }

    public NakadiClientBuilder withBackoffStrategy(final BackoffStrategy backoffStrategy){
        return new NakadiClientBuilder(baseUri, objectMapper, authorizationProvider, clientHttpRequestFactory, cursorManager, eventPublishingHandlers, backoffStrategy);
    }

    static RequestFactory wrapClientHttpRequestFactory(RequestFactory delegate, @Nullable AuthorizationProvider authorizationProvider) {
        RequestFactory requestFactory = new ProblemHandlingRequestFactory(new UserAgentRequestFactory(delegate));
        if (authorizationProvider != null) {
            requestFactory = new AuthorizedRequestFactory(requestFactory, authorizationProvider);
        }

        return requestFactory;
    }

    /**
     * Creates a new instance of {@code NakadiClient}. In case no {@code ObjectMapper} is provided, it's going to make
     * use of {@code DefaultObjectMapper} that is making use of
     * {@code PropertyNamingStrategies.SNAKE_CASE}.
     * In case no {@code CursorManager} is provided it's going to make use of {@code ManagedCursorManager}.
     *
     * @return A fresh instance of {@code NakadiClient}
     */
    public NakadiClient build() {
        final RequestFactory clientHttpRequestFactory = wrapClientHttpRequestFactory(this.clientHttpRequestFactory, authorizationProvider);
        final CursorManager cursorManager = this.cursorManager != null ? this.cursorManager : new ManagedCursorManager(baseUri, clientHttpRequestFactory, true);
        final ObjectMapper objectMapper = this.objectMapper != null ? this.objectMapper : DefaultObjectMapper.INSTANCE;
        final BackoffStrategy backoffStrategy = this.backoffStrategy != null ? this.backoffStrategy : new ExponentialBackoffStrategy();

        return new NakadiClient(
                baseUri,
                clientHttpRequestFactory,
                objectMapper,
                cursorManager,
                eventPublishingHandlers,
                backoffStrategy
        );
    }
}
