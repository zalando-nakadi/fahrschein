package org.zalando.fahrschein;

import com.google.common.collect.Iterables;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.metrics.MetricsCollector;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.zalando.fahrschein.metrics.NoMetricsCollector.NO_METRICS_COLLECTOR;

public abstract class PreparedListening<T> {

    protected final NakadiReaderFactory nakadiReaderFactory;
    protected final URI baseUri;
    protected StreamParameters streamParameters = new StreamParameters();
    protected final Class<T> eventType;
    protected final Listener<T> listener;
    protected MetricsCollector metricsCollector = NO_METRICS_COLLECTOR;

    protected PreparedListening(final NakadiReaderFactory nakadiReaderFactory, final URI baseUri, final Class<T> eventType, final Listener<T> listener) {
        this.nakadiReaderFactory = nakadiReaderFactory;
        this.baseUri = baseUri;
        this.eventType = eventType;
        this.listener = listener;
    }

    public PreparedListening withStreamParameters(final StreamParameters streamParameters) {
        this.streamParameters = streamParameters;
        return this;
    }

    public PreparedListening withMetricCollector(final MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
        return this;
    }

    public abstract void startListening() throws IOException;

    /* LowLevelApiPrepareListening */
    public static class LowLevelApiPreparedListening<T> extends PreparedListening<T> {

        private final String eventName;

        LowLevelApiPreparedListening(final NakadiReaderFactory nakadiReaderFactory, final URI baseUri, final Class<T> eventType, final Listener<T> listener, final String eventName) {
            super(nakadiReaderFactory, baseUri, eventType, listener);
            this.eventName = eventName;
        }

        @Override
        public void startListening() throws IOException {
            final String queryString = streamParameters.toQueryString();
            final URI uri = baseUri.resolve(String.format("/event-types/%s/events?%s", eventName, queryString));
            final NakadiReader<T> nakadiReader = nakadiReaderFactory.createReader(uri, eventName, Optional.<Subscription>empty(), eventType, listener, metricsCollector);
            nakadiReader.run();
        }
    }

    /* SubscriptionApiPrepareListening */
    public static class SubscriptionApiPreparedListening<T> extends PreparedListening<T> {

        private final Subscription subscription;

        protected SubscriptionApiPreparedListening(final NakadiReaderFactory nakadiReaderFactory, final URI baseUri, final Class<T> eventType, final Listener<T> listener, final Subscription subscription) {
            super(nakadiReaderFactory, baseUri, eventType, listener);
            this.subscription = subscription;
        }

        @Override
        public void startListening() throws IOException {
            final String eventName = Iterables.getOnlyElement(subscription.getEventTypes());
            final String queryString = streamParameters.toQueryString();
            final URI uri = baseUri.resolve(String.format("/subscriptions/%s/events?%s", subscription.getId(), queryString));
            final NakadiReader<T> nakadiReader = nakadiReaderFactory.createReader(uri, eventName, Optional.of(subscription), eventType, listener, metricsCollector);
            nakadiReader.run();
        }
    }

}
