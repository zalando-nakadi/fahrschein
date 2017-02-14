package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class StreamBuilders {
    abstract static class AbstractStreamBuilder implements StreamBuilder {
        protected final URI baseUri;
        protected final ClientHttpRequestFactory clientHttpRequestFactory;
        protected final CursorManager cursorManager;
        protected final ObjectMapper objectMapper;
        @Nullable
        protected final BackoffStrategy backoffStrategy;
        @Nullable
        protected final StreamParameters streamParameters;
        @Nullable
        protected final MetricsCollector metricsCollector;
        @Nullable
        protected final ErrorHandler errorHandler;

        protected AbstractStreamBuilder(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager, ObjectMapper objectMapper, @Nullable BackoffStrategy backoffStrategy, @Nullable StreamParameters streamParameters, @Nullable ErrorHandler errorHandler, @Nullable MetricsCollector metricsCollector) {
            this.baseUri = baseUri;
            this.clientHttpRequestFactory = clientHttpRequestFactory;
            this.cursorManager = cursorManager;
            this.backoffStrategy = backoffStrategy;
            this.objectMapper = objectMapper;
            this.streamParameters = streamParameters;
            this.errorHandler = errorHandler;
            this.metricsCollector = metricsCollector;
        }

        protected abstract URI getURI(String queryString);
        protected abstract Set<String> getEventNames();
        @Nullable
        protected abstract Subscription getSubscription();
        @Nullable
        protected abstract Lock getLock();

        @Override
        public final <T> void listen(Class<T> eventClass, Listener<T> listener) throws IOException {
            runnable(eventClass, listener).run();
        }

        @Override
        public final <T> IORunnable runnable(Class<T> eventClass, Listener<T> listener) {
            final StreamParameters streamParameters = this.streamParameters != null ? this.streamParameters : new StreamParameters();
            final String queryString = streamParameters.toQueryString();

            final URI uri = getURI(queryString);
            final Set<String> eventNames = getEventNames();
            final Subscription subscription = getSubscription();
            final Lock lock = getLock();

            final BackoffStrategy backoffStrategy = this.backoffStrategy != null ? this.backoffStrategy : new ExponentialBackoffStrategy();
            final MetricsCollector metricsCollector = this.metricsCollector != null ? this.metricsCollector : NoMetricsCollector.NO_METRICS_COLLECTOR;
            final ErrorHandler errorHandler = this.errorHandler != null ? this.errorHandler : DefaultErrorHandler.INSTANCE;
            final ObjectMapper objectMapper = this.objectMapper != null ? this.objectMapper : DefaultObjectMapper.INSTANCE;

            return new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper,
                    eventNames, subscription, lock, eventClass, listener, errorHandler, metricsCollector);
        }

    }

    static class SubscriptionStreamBuilderImpl extends AbstractStreamBuilder implements StreamBuilder.SubscriptionStreamBuilder {
        private final Subscription subscription;

        SubscriptionStreamBuilderImpl(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager, ObjectMapper objectMapper, Subscription subscription) {
            this(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, null, null, null, null, subscription);
        }

        private SubscriptionStreamBuilderImpl(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager, ObjectMapper objectMapper, @Nullable BackoffStrategy backoffStrategy, @Nullable StreamParameters streamParameters, @Nullable ErrorHandler errorHandler, @Nullable MetricsCollector metricsCollector, Subscription subscription) {
            super(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector);
            this.subscription = subscription;
        }

        @Override
        protected URI getURI(String queryString) {
            return baseUri.resolve(String.format("/subscriptions/%s/events?%s", subscription.getId(), queryString));
        }

        @Override
        protected Set<String> getEventNames() {
            return subscription.getEventTypes();
        }

        @Override
        protected Subscription getSubscription() {
            return subscription;
        }

        @Override
        protected Lock getLock() {
            return null;
        }

        @Override
        public SubscriptionStreamBuilder withBackoffStrategy(BackoffStrategy backoffStrategy) {
            return new SubscriptionStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector, subscription);
        }

        @Override
        public SubscriptionStreamBuilder withErrorHandler(ErrorHandler errorHandler) {
            return new SubscriptionStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector, subscription);
        }

        @Override
        public SubscriptionStreamBuilder withMetricsCollector(MetricsCollector metricsCollector) {
            return new SubscriptionStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector, subscription);
        }

        @Override
        public SubscriptionStreamBuilder withStreamParameters(StreamParameters streamParameters) {
            return new SubscriptionStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector, subscription);
        }

        @Override
        public SubscriptionStreamBuilder withObjectMapper(ObjectMapper objectMapper) {
            return new SubscriptionStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector, subscription);
        }
    }

    static class LowLevelStreamBuilderImpl extends AbstractStreamBuilder implements StreamBuilder.LowLevelStreamBuilder {
        private final String eventName;
        private final Lock lock;

        LowLevelStreamBuilderImpl(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager, ObjectMapper objectMapper, String eventName) {
            this(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, null, null, null, null, eventName, null);
        }

        private LowLevelStreamBuilderImpl(URI baseUri, ClientHttpRequestFactory clientHttpRequestFactory, CursorManager cursorManager, ObjectMapper objectMapper, @Nullable BackoffStrategy backoffStrategy, @Nullable StreamParameters streamParameters, @Nullable ErrorHandler errorHandler, @Nullable MetricsCollector metricsCollector, String eventName, @Nullable Lock lock) {
            super(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector);
            this.eventName = eventName;
            this.lock = lock;
        }

        @Override
        protected URI getURI(String queryString) {
            return baseUri.resolve(String.format("/event-types/%s/events?%s", eventName, queryString));
        }

        @Override
        protected Set<String> getEventNames() {
            return Collections.singleton(eventName);
        }

        @Override
        protected Subscription getSubscription() {
            return null;
        }

        @Override
        protected Lock getLock() {
            return lock;
        }

        @Override
        public LowLevelStreamBuilder withBackoffStrategy(BackoffStrategy backoffStrategy) {
            return new LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withMetricsCollector(MetricsCollector metricsCollector) {
            return new LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withErrorHandler(ErrorHandler errorHandler) {
            return new LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withStreamParameters(StreamParameters streamParameters) {
            return new LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withObjectMapper(ObjectMapper objectMapper) {
            return new LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withLock(Lock lock) {
            return new LowLevelStreamBuilderImpl(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, errorHandler, metricsCollector, eventName, lock);
        }

        /**
         * Initializes offsets to start streaming at the oldest available offset (BEGIN).
         */
        public LowLevelStreamBuilder readFromBegin(List<Partition> partitions) throws IOException {
            final List<Cursor> cursors = new ArrayList<>(partitions.size());
            for (Partition partition : partitions) {
                cursors.add(new Cursor(partition.getPartition(), "BEGIN"));
            }

            cursorManager.onSuccess(eventName, cursors);
            return this;
        }

        /**
         * Updates cursors to the newest available offset.
         * This is similar to the default behaviour, but allows to specify the partitions to stream from instead of getting events from all partitions.
         */
        public LowLevelStreamBuilder readFromNewestAvailableOffset(List<Partition> partitions) throws IOException {
            final List<Cursor> cursors = new ArrayList<>(partitions.size());
            for (Partition partition : partitions) {
                cursors.add(new Cursor(partition.getPartition(), partition.getNewestAvailableOffset()));
            }

            cursorManager.onSuccess(eventName, cursors);
            return this;
        }

        /**
         * Updates cursors in case the currently stored offset is no longer available. Streaming will start at the oldest available offset (BEGIN) to minimize the amount of events skipped.
         */
        public LowLevelStreamBuilder skipUnavailableOffsets(List<Partition> partitions) throws IOException {


            final Collection<Cursor> cursors = cursorManager.getCursors(eventName);
            final Map<String, Cursor> cursorsByPartition = new HashMap<>();
            for (Cursor cursor : cursors) {
                cursorsByPartition.put(cursor.getPartition(), cursor);
            }

            final List<Cursor> newCursors = new ArrayList<>(partitions.size());
            for (Partition partition : partitions) {
                final Cursor cursor = cursorsByPartition.get(partition.getPartition());
                if (isNoLongerAvailable(cursorsByPartition, partition)) {
                    newCursors.add(new Cursor(partition.getPartition(), "BEGIN"));
                }
            }

            if (!newCursors.isEmpty()) {
                cursorManager.onSuccess(eventName, newCursors);
            }

            return this;
        }

        private static boolean isNoLongerAvailable(Map<String, Cursor> cursorsByPartition, Partition p) {
            final Cursor cursor = cursorsByPartition.get(p.getPartition());
            return (cursor == null || (!"BEGIN".equals(cursor.getOffset()) && OffsetComparator.INSTANCE.compare(cursor.getOffset(), p.getOldestAvailableOffset()) < 0));
        }
    }
}
