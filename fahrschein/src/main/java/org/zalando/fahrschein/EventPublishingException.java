package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.BatchItemResponse;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

/**
 * Thrown in case the client wasn't able to publish full batch of events to Nakadi.
 *
 * The response will contain an array of all {@code BatchItemResponse}s, independent of their status.
 * There is an ordering guarantee from Nakadi, so that you can correlate the elements in the response
 * with your input batch, and potentially retry only the failed events. Every record includes the eid (event id) which can be used to identify the event.
 *
 */
public class EventPublishingException extends IOException {
    private final BatchItemResponse[] responses;

    public EventPublishingException(BatchItemResponse[] responses) {
        super(formatMessage(responses));
        this.responses = responses;
    }

    private static String formatMessage(BatchItemResponse[] responses) {
        final Formatter fmt = new Formatter(Locale.ROOT);
        for (int i = 0; i < responses.length; i++) {
            final BatchItemResponse res = responses[i];
            fmt.format("Event publishing of [%s] returned status [%s] in step [%s] with detail [%s]%s",
                    res.getEid(),
                    res.getPublishingStatus(),
                    res.getStep(),
                    res.getDetail(),
                    i < responses.length-1 ? ", " : "");
        }
        return fmt.toString();
    }

    public BatchItemResponse[] getResponses() {
        return responses;
    }
}
