package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.http.api.RequestFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

class StreamBuilders {
    abstract static class AbstractStreamBuilder implements StreamBuilder {
        protected final URI baseUri;
        protected final RequestFactory requestFactory;
        protected final CursorManager cursorManager;
        protected final ObjectMapper objectMapper;
        @Nullable
        protected final BackoffStrategy backoffStrategy;
        @Nullable
        protected final StreamParameters streamParameters;
        @Nullable
        protected final BatchHandler batchHandler;
        @Nullable
        protected final MetricsCollector metricsCollector;

        protected AbstractStreamBuilder(URI baseUri, RequestFactory requestFactory, CursorManager cursorManager, ObjectMapper objectMapper, @Nullable BackoffStrategy backoffStrategy, @Nullable StreamParameters streamParameters, @Nullable BatchHandler batchHandler, @Nullable MetricsCollector metricsCollector) {
            this.baseUri = baseUri;
            this.requestFactory = requestFactory;
            this.cursorManager = cursorManager;
            this.backoffStrategy = backoffStrategy;
            this.objectMapper = objectMapper;
            this.streamParameters = streamParameters;
            this.batchHandler = batchHandler;
            this.metricsCollector = metricsCollector;
        }

        protected abstract URI getURI(String queryString);
        protected abstract Set<String> getEventNames();
        protected abstract Optional<Subscription> getSubscription();
        protected abstract Optional<Lock> getLock();

        @Override
        public final <T> void listen(Class<T> eventClass, Listener<T> listener) throws IOException {
            runnable(eventClass, listener).run();
        }

        @Override
        public final <T> void listen(EventReader<T> eventReader, Listener<T> listener) throws IOException {
            runnable(eventReader, listener).run();
        }

        @Override
        public final <T> IORunnable runnable(EventReader<T> eventReader, Listener<T> listener) {
            final StreamParameters streamParameters = this.streamParameters != null ? this.streamParameters : new StreamParameters();
            final String queryString = streamParameters.toQueryString();

            final URI uri = getURI(queryString);
            final Set<String> eventNames = getEventNames();
            final Optional<Subscription> subscription = getSubscription();
            final Optional<Lock> lock = getLock();

            final BackoffStrategy backoffStrategy = this.backoffStrategy != null ? this.backoffStrategy : new EqualJitterBackoffStrategy();
            final MetricsCollector metricsCollector = this.metricsCollector != null ? this.metricsCollector : NoMetricsCollector.NO_METRICS_COLLECTOR;
            final BatchHandler batchHandler = this.batchHandler != null ? this.batchHandler : DefaultBatchHandler.INSTANCE;

            return new NakadiReader<>(
                    uri, requestFactory, backoffStrategy, cursorManager,
                    eventNames, subscription, lock, eventReader, listener, batchHandler,
                    metricsCollector, StreamInfoReader.getDefault());
        }

        @Override
        public final <T> IORunnable runnable(Class<T> eventClass, Listener<T> listener) {
            final EventReader<T> eventReader = new MappingEventReader<>(eventClass, objectMapper);
            return runnable(eventReader, listener);
        }

    }

    static class SubscriptionStreamBuilderImpl extends AbstractStreamBuilder implements StreamBuilder.SubscriptionStreamBuilder {
        private final Subscription subscription;

        SubscriptionStreamBuilderImpl(URI baseUri, RequestFactory clientHttpRequestFactory, CursorManager cursorManager, ObjectMapper objectMapper, Subscription subscription) {
            this(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, null, null, null, null, subscription);
        }

        private SubscriptionStreamBuilderImpl(URI baseUri, RequestFactory clientHttpRequestFactory, CursorManager cursorManager, ObjectMapper objectMapper, @Nullable BackoffStrategy backoffStrategy, @Nullable StreamParameters streamParameters, @Nullable BatchHandler batchHandler, @Nullable MetricsCollector metricsCollector, Subscription subscription) {
            super(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector);
            this.subscription = subscription;
        }

        @Override
        protected URI getURI(String queryString) {
            return baseUri.resolve("/subscriptions/" + subscription.getId() + "/events?" + queryString);
        }

        @Override
        protected Set<String> getEventNames() {
            return subscription.getEventTypes();
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
            return new SubscriptionStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector, subscription);
        }

        @Override
        public SubscriptionStreamBuilder withBatchHandler(BatchHandler batchHandler) {
            return new SubscriptionStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector, subscription);
        }

        @Override
        public SubscriptionStreamBuilder withMetricsCollector(MetricsCollector metricsCollector) {
            return new SubscriptionStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector, subscription);
        }

        @Override
        public SubscriptionStreamBuilder withStreamParameters(StreamParameters streamParameters) {
            return new SubscriptionStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector, subscription);
        }

        @Override
        public SubscriptionStreamBuilder withObjectMapper(ObjectMapper objectMapper) {
            return new SubscriptionStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector, subscription);
        }
    }

    static class LowLevelStreamBuilderImpl extends AbstractStreamBuilder implements StreamBuilder.LowLevelStreamBuilder {
        private final String eventName;
        private final Lock lock;

        LowLevelStreamBuilderImpl(URI baseUri, RequestFactory clientHttpRequestFactory, CursorManager cursorManager, ObjectMapper objectMapper, String eventName) {
            this(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, null, null, null, null, eventName, null);
        }

        private LowLevelStreamBuilderImpl(URI baseUri, RequestFactory clientHttpRequestFactory, CursorManager cursorManager, ObjectMapper objectMapper, @Nullable BackoffStrategy backoffStrategy, @Nullable StreamParameters streamParameters, @Nullable BatchHandler batchHandler, @Nullable MetricsCollector metricsCollector, String eventName, @Nullable Lock lock) {
            super(baseUri, clientHttpRequestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector);
            this.eventName = eventName;
            this.lock = lock;
        }

        @Override
        protected URI getURI(String queryString) {
            return baseUri.resolve("/event-types/" + eventName + "/events?" + queryString);
        }

        @Override
        protected Set<String> getEventNames() {
            return Collections.singleton(eventName);
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
            return new LowLevelStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withMetricsCollector(MetricsCollector metricsCollector) {
            return new LowLevelStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withBatchHandler(BatchHandler batchHandler) {
            return new LowLevelStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withStreamParameters(StreamParameters streamParameters) {
            return new LowLevelStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withObjectMapper(ObjectMapper objectMapper) {
            return new LowLevelStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector, eventName, lock);
        }

        @Override
        public LowLevelStreamBuilder withLock(Lock lock) {
            return new LowLevelStreamBuilderImpl(baseUri, requestFactory, cursorManager, objectMapper, backoffStrategy, streamParameters, batchHandler, metricsCollector, eventName, lock);
        }

        /**
         * Initializes offsets to start streaming at the oldest available offset (BEGIN).
         */
        public LowLevelStreamBuilder readFromBegin(List<Partition> partitions) throws IOException {
            final List<Cursor> cursors = partitions.stream().map(p -> new Cursor(p.getPartition(), "BEGIN")).collect(toList());
            cursorManager.onSuccess(eventName, cursors);
            return this;
        }

        /**
         * Updates cursors to the newest available offset.
         * This is similar to the default behaviour, but allows to specify the partitions to stream from instead of getting events from all partitions.
         */
        public LowLevelStreamBuilder readFromNewestAvailableOffset(List<Partition> partitions) throws IOException {
            final List<Cursor> cursors = partitions.stream().map(p -> new Cursor(p.getPartition(), p.getNewestAvailableOffset())).collect(toList());
            cursorManager.onSuccess(eventName, cursors);
            return this;
        }

        /**
         * Updates cursors in case the currently stored offset is no longer available. Streaming will start at the oldest available offset (BEGIN) to minimize the amount of events skipped.
         */
        public LowLevelStreamBuilder skipUnavailableOffsets(List<Partition> partitions) throws IOException {
            final Map<String, Cursor> cursorsByPartition = cursorManager.getCursors(eventName).stream().collect(toMap(Cursor::getPartition, identity()));
            final List<Cursor> cursors = partitions.stream().filter(p -> isNoLongerAvailable(cursorsByPartition, p)).map(p -> new Cursor(p.getPartition(), "BEGIN")).collect(toList());

            if (!cursors.isEmpty()) {
                cursorManager.onSuccess(eventName, cursors);
            }

            return this;
        }

        private static boolean isNoLongerAvailable(Map<String, Cursor> cursorsByPartition, Partition p) {
            final Cursor cursor = cursorsByPartition.get(p.getPartition());
            return (cursor == null || (!"BEGIN".equals(cursor.getOffset()) && OffsetComparator.INSTANCE.compare(cursor.getOffset(), p.getOldestAvailableOffset()) < 0));
        }
    }
}
