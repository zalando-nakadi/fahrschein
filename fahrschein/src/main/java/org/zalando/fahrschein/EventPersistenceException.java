package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.BatchItemResponse;

import java.util.List;

/**
 * <p>Exception thrown for server-side (partial) failures of event persistence, e.g. an event can not be stored due to Kafka unavailability.</p>
 *
 * <p>The exception contains an array of all {@link BatchItemResponse}s, independent of their status.
 * There is an ordering guarantee from Nakadi, so that you can correlate the elements in the response
 * with your input batch, and potentially retry only the failed or aborted events. Every record includes the eid ({@link BatchItemResponse#getEid()})
 * which can also be used to identify the event.
 * </p>
 */
public class EventPersistenceException extends EventPublishingException {

    public EventPersistenceException(BatchItemResponse[] responses)
    {
        super(responses);
    }

}
