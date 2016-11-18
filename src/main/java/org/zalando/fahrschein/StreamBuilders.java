package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.metrics.MetricsCollector;
import org.zalando.fahrschein.metrics.NoMetricsCollector;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static java.util.Optional.ofNullable;

class StreamBuilders {
    abstract static class AbstractStreamBuilder implements StreamBuilder {
        protected final URI baseUri;
        protected final ClientHttpRequestFactory clientHttpRequestFactory;
        protected final CursorManager cursorManager;
        @Nullable
        protected final BackoffStrategy backoffStrategy;
        @Nullable
        protected final ObjectMapper objectMapper;
        @Nullable
        protected final StreamParameters streamParameters;
        @Nullable
        protected final MetricsCollector metricsCollector;

        protected AbstractStreamBuilder(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager, @Nullable BackoffStrategy backoffStrategy, @Nullable ObjectMapper objectMapper, @Nullable StreamParameters streamParameters, @Nullable MetricsCollector metricsCollector) {
            this.baseUri = baseUri;
            this.clientHttpRequestFactory = clientHttpRequestFactory;
            this.cursorManager = cursorManager;
            this.backoffStrategy = backoffStrategy;
            this.objectMapper = objectMapper;
            this.streamParameters = streamParameters;
            this.metricsCollector = metricsCollector;
        }

        protected abstract URI getURI(String queryString);
        protected abstract String getEventName();
        protected abstract Optional<Subscription> getSubscription();
        protected abstract Optional<Lock> getLock();

        @Override
        public final <T> void listen(Class<T> eventClass, Listener<T> listener) throws IOException {
            final StreamParameters streamParameters = ofNullable(this.streamParameters).orElseGet(StreamParameters::new);
            final String queryString = streamParameters.toQueryString();

            final URI uri = getURI(queryString);
            final String eventName = getEventName();
            final Optional<Subscription> subscription = getSubscription();
            final Optional<Lock> lock = getLock();

            final BackoffStrategy backoffStrategy = ofNullable(this.backoffStrategy).orElseGet(ExponentialBackoffStrategy::new);
            final ObjectMapper objectMapper = ofNullable(this.objectMapper).orElse(DefaultObjectMapper.INSTANCE);
            final MetricsCollector metricsCollector = ofNullable(this.metricsCollector).orElse(NoMetricsCollector.NO_METRICS_COLLECTOR);

            final NakadiReader<T> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, eventName, subscription, lock, eventClass, listener, metricsCollector);

            nakadiReader.run();
        }

    }

    static class SubscriptionStreamBuilderImpl extends AbstractStreamBuilder implements StreamBuilder.SubscriptionStreamBuilder {
        private final Subscription subscription;

        SubscriptionStreamBuilderImpl(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager, Subscription subscription) {
            this(baseUri, clientHttpRequestFactory, cursorManager, subscription, null, null, null, null);
        }

        private SubscriptionStreamBuilderImpl(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager, Subscription subscription, @Nullable BackoffStrategy backoffStrategy, @Nullable ObjectMapper objectMapper, @Nullable StreamParameters streamParameters, @Nullable MetricsCollector metricsCollector) {
            super(baseUri, clientHttpRequestFactory, cursorManager, backoffStrategy, objectMapper, streamParameters, metricsCollector);
            this.subscription = subscription;
        }

        @Override
        protected URI getURI(String queryString) {
            final String eventName = getEventName();
            return baseUri.resolve(String.format("/event-types/%s/events?%s", eventName, queryString));
        }

        @Override
        protected String getEventName() {
            return Iterables.getOnlyElement(subscription.getEventTypes());
        }

        @Override
        protected Optional<Subscription> getSubscription() {
            return Optional.of(subscription);
        }

        @Override
        protected Optional<Lock> getLock() {
            return Optional.empty();
        }

        @Override
        public SubscriptionStreamBuilder withBackoffStrategy(BackoffStrategy backoffStrategy) {
            return new SubscriptionStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, subscription, backoffStrategy, objectMapper, streamParameters, metricsCollector);
        }

        @Override
        public SubscriptionStreamBuilder withMetricsCollector(MetricsCollector metricsCollector) {
            return new SubscriptionStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, subscription, backoffStrategy, objectMapper, streamParameters, metricsCollector);
        }

        @Override
        public SubscriptionStreamBuilder withStreamParameters(StreamParameters streamParameters) {
            return new SubscriptionStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, subscription, backoffStrategy, objectMapper, streamParameters, metricsCollector);
        }

        @Override
        public StreamBuilder withObjectMapper(ObjectMapper objectMapper) {
            return new SubscriptionStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, subscription, backoffStrategy, objectMapper, streamParameters, metricsCollector);
        }
    }

    static class LowLevelStreamBuilderImpl extends AbstractStreamBuilder implements StreamBuilder.LowLevelStreamBuilder {
        private final String eventName;
        private final Lock lock;

        LowLevelStreamBuilderImpl(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager,  String eventName) {
            this(baseUri, clientHttpRequestFactory, cursorManager, null, null, null, null, eventName, null);
        }

        private LowLevelStreamBuilderImpl(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager, @Nullable BackoffStrategy backoffStrategy, @Nullable ObjectMapper objectMapper, @Nullable StreamParameters streamParameters, @Nullable MetricsCollector metricsCollector, String eventName, @Nullable Lock lock) {
            super(baseUri, clientHttpRequestFactory, cursorManager, backoffStrategy, objectMapper, streamParameters, metricsCollector);
            this.eventName = eventName;
            this.lock = lock;
        }

        @Override
        protected URI getURI(String queryString) {
            return baseUri.resolve(String.format("/event-types/%s/events?%s", eventName, queryString));
        }

        @Override
        protected String getEventName() {
            return eventName;
        }

        @Override
        protected Optional<Subscription> getSubscription() {
            return Optional.empty();
        }

        @Override
        protected Optional<Lock> getLock() {
            return ofNullable(lock);
        }

        @Override
        public LowLevelStreamBuilder withBackoffStrategy(BackoffStrategy backoffStrategy) {
            return new LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, backoffStrategy, objectMapper, streamParameters, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withMetricsCollector(MetricsCollector metricsCollector) {
            return new LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, backoffStrategy, objectMapper, streamParameters, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withStreamParameters(StreamParameters streamParameters) {
            return new LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, backoffStrategy, objectMapper, streamParameters, metricsCollector, eventName, lock);
        }

        @Override
        public StreamBuilder withObjectMapper(ObjectMapper objectMapper) {
            return new LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, backoffStrategy, objectMapper, streamParameters, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withLock(Lock lock) {
            return new LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, backoffStrategy, objectMapper, streamParameters, metricsCollector, eventName, lock);
        }
    }
}
