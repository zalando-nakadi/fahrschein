package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import java.io.IOException;
import java.util.List;

public interface StreamBuilder {

    interface SubscriptionStreamBuilder extends StreamBuilder {
        @Override
        SubscriptionStreamBuilder withBackoffStrategy(BackoffStrategy backoffStrategy);
        @Override
        SubscriptionStreamBuilder withMetricsCollector(MetricsCollector metricsCollector);
        @Override
        SubscriptionStreamBuilder withErrorHandler(ErrorHandler errorHandler);
        @Override
        SubscriptionStreamBuilder withStreamParameters(StreamParameters streamParameters);
    }

    interface LowLevelStreamBuilder extends StreamBuilder {
        @Override
        LowLevelStreamBuilder withBackoffStrategy(BackoffStrategy backoffStrategy);
        @Override
        LowLevelStreamBuilder withMetricsCollector(MetricsCollector metricsCollector);
        @Override
        LowLevelStreamBuilder withErrorHandler(ErrorHandler errorHandler);
        @Override
        LowLevelStreamBuilder withStreamParameters(StreamParameters streamParameters);

        LowLevelStreamBuilder withLock(Lock lock);

        LowLevelStreamBuilder readFromBegin(List<Partition> partitions) throws IOException;
        LowLevelStreamBuilder readFromNewestAvailableOffset(List<Partition> partitions) throws IOException;
        LowLevelStreamBuilder skipUnavailableOffsets(List<Partition> partitions) throws IOException;
    }

    StreamBuilder withErrorHandler(ErrorHandler errorHandler);

    StreamBuilder withMetricsCollector(MetricsCollector metricsCollector);

    StreamBuilder withStreamParameters(StreamParameters streamParameters);

    StreamBuilder withObjectMapper(ObjectMapper objectMapper);

    StreamBuilder withBackoffStrategy(BackoffStrategy backoffStrategy);

    <T> IORunnable runnable(Class<T> eventClass, Listener<T> listener);
    <T> void listen(Class<T> eventClass, Listener<T> listener) throws IOException;

}
