package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.fahrschein.domain.Lock;

import java.io.IOException;

public interface StreamBuilder {

    interface SubscriptionStreamBuilder extends StreamBuilder {
        @Override
        SubscriptionStreamBuilder withBackoffStrategy(BackoffStrategy backoffStrategy);
        @Override
        SubscriptionStreamBuilder withMetricsCollector(MetricsCollector metricsCollector);
        @Override
        SubscriptionStreamBuilder withStreamParameters(StreamParameters streamParameters);
    }

    interface LowLevelStreamBuilder extends StreamBuilder {
        @Override
        LowLevelStreamBuilder withBackoffStrategy(BackoffStrategy backoffStrategy);
        @Override
        LowLevelStreamBuilder withMetricsCollector(MetricsCollector metricsCollector);
        @Override
        LowLevelStreamBuilder withStreamParameters(StreamParameters streamParameters);

        LowLevelStreamBuilder withLock(Lock lock);
    }

    StreamBuilder withMetricsCollector(MetricsCollector metricsCollector);

    StreamBuilder withStreamParameters(StreamParameters streamParameters);

    StreamBuilder withObjectMapper(ObjectMapper objectMapper);

    StreamBuilder withBackoffStrategy(BackoffStrategy backoffStrategy);

    <T> void listen(Class<T> eventClass, Listener<T> listener) throws IOException;

}
