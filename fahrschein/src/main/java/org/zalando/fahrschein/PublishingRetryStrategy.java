package org.zalando.fahrschein;

import java.util.List;

public interface PublishingRetryStrategy {
    <T> List<T> getEventsForRetry(final EnrichedEventPersistenceException ex);
}
