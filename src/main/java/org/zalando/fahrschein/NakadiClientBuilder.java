package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.zalando.fahrschein.metrics.MetricsCollector;
import org.zalando.fahrschein.metrics.NoMetricsCollector;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.ofNullable;

public final class NakadiClientBuilder {
    private final URI baseUri;
    private AccessTokenProvider accessTokenProvider;
    private ClientHttpRequestFactory clientHttpRequestFactory;
    private BackoffStrategy backoffStrategy;
    private ObjectMapper objectMapper;
    private CursorManager cursorManager;
    private MetricsCollector metricsCollector;
    private StreamParameters streamParameters;

    NakadiClientBuilder(final URI baseUri) {
        this.baseUri = checkNotNull(baseUri, "Base URI should not be null");
    }

    private NakadiClientBuilder(URI baseUri, AccessTokenProvider accessTokenProvider, ClientHttpRequestFactory clientHttpRequestFactory, BackoffStrategy backoffStrategy, ObjectMapper objectMapper, CursorManager cursorManager, MetricsCollector metricsCollector, StreamParameters streamParameters) {
        this.baseUri = baseUri;
        this.accessTokenProvider = accessTokenProvider;
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.backoffStrategy = backoffStrategy;
        this.objectMapper = objectMapper;
        this.cursorManager = cursorManager;
        this.metricsCollector = metricsCollector;
        this.streamParameters = streamParameters;
    }

    public NakadiClientBuilder withAccessTokenProvider(AccessTokenProvider accessTokenProvider) {
        return new NakadiClientBuilder(baseUri, accessTokenProvider, clientHttpRequestFactory, backoffStrategy, objectMapper, cursorManager, metricsCollector, streamParameters);
    }

    public NakadiClientBuilder withClientHttpRequestFactory(ClientHttpRequestFactory clientHttpRequestFactory) {
        return new NakadiClientBuilder(baseUri, accessTokenProvider, clientHttpRequestFactory, backoffStrategy, objectMapper, cursorManager, metricsCollector, streamParameters);
    }

    public NakadiClientBuilder withBackoffStrategy(BackoffStrategy backoffStrategy) {
        return new NakadiClientBuilder(baseUri, accessTokenProvider, clientHttpRequestFactory, backoffStrategy, objectMapper, cursorManager, metricsCollector, streamParameters);
    }

    public NakadiClientBuilder withObjectMapper(ObjectMapper objectMapper) {
        return new NakadiClientBuilder(baseUri, accessTokenProvider, clientHttpRequestFactory, backoffStrategy, objectMapper, cursorManager, metricsCollector, streamParameters);
    }

    public NakadiClientBuilder withCursorManager(CursorManager cursorManager) {
        return new NakadiClientBuilder(baseUri, accessTokenProvider, clientHttpRequestFactory, backoffStrategy, objectMapper, cursorManager, metricsCollector, streamParameters);
    }

    public NakadiClientBuilder withMetricsCollector(MetricsCollector metricsCollector) {
        return new NakadiClientBuilder(baseUri, accessTokenProvider, clientHttpRequestFactory, backoffStrategy, objectMapper, cursorManager, metricsCollector, streamParameters);
    }

    public NakadiClientBuilder withStreamParameters(StreamParameters streamParameters) {
        return new NakadiClientBuilder(baseUri, accessTokenProvider, clientHttpRequestFactory, backoffStrategy, objectMapper, cursorManager, metricsCollector, streamParameters);
    }

    private ClientHttpRequestFactory defaultClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory requestFactoryDelegate = new SimpleClientHttpRequestFactory();
        requestFactoryDelegate.setConnectTimeout(400);
        requestFactoryDelegate.setReadTimeout(60*1000);

        ClientHttpRequestFactory requestFactory = new ProblemHandlingClientHttpRequestFactory(requestFactoryDelegate);
        if (accessTokenProvider != null) {
            requestFactory = new AuthorizedClientHttpRequestFactory(requestFactoryDelegate, accessTokenProvider);
        }

        return requestFactory;
    }

    public NakadiClient build() {
        final ClientHttpRequestFactory clientHttpRequestFactory = ofNullable(this.clientHttpRequestFactory).orElseGet(this::defaultClientHttpRequestFactory);
        final BackoffStrategy backoffStrategy = ofNullable(this.backoffStrategy).orElseGet(ExponentialBackoffStrategy::new);
        final ObjectMapper objectMapper = ofNullable(this.objectMapper).orElse(DefaultObjectMapper.INSTANCE);
        final CursorManager cursorManager = ofNullable(this.cursorManager).orElseGet(() -> new ManagedCursorManager(baseUri, clientHttpRequestFactory));
        final MetricsCollector metricsCollector = ofNullable(this.metricsCollector).orElse(NoMetricsCollector.NO_METRICS_COLLECTOR);

        // TODO: Pass StreamParameters
        return new NakadiClient(baseUri, clientHttpRequestFactory, backoffStrategy, objectMapper, cursorManager, metricsCollector);
    }
}
