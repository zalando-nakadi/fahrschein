package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.BatchItemResponse;

import java.util.List;

/**
 * <p>Exception thrown during publishing of events, in case the batch fails the validation phase.
 * </p>
 * <p>The exception will contain an array of all {@link BatchItemResponse}s, independent of their status.
 * There is an ordering guarantee from Nakadi, so that you can correlate the elements in the response
 * with your input batch, and potentially retry only the failed or aborted events. Every record includes the eid ({@link BatchItemResponse#getEid()})
 * which can also be used to identify the event.
 * </p>
 */
public class EventValidationException extends EventPublishingException {

    public EventValidationException(BatchItemResponse[] responses)
    {
        super(responses);
    }
}
