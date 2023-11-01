package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.BatchItemResponse;

/**
 * <p>Exception thrown for server-side (partial) failures of event persistence, e.g. an event can not be stored due to Kafka unavailability.</p>
 *
 * <p>Not to be used publicly, please use the wrapping {@link EventPersistenceException} class.</p>
 */
public class RawEventPersistenceException extends EventPublishingException {

    public RawEventPersistenceException(BatchItemResponse[] responses)
    {
        super(responses);
    }

}
