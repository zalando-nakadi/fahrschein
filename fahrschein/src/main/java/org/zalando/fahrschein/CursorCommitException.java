package org.zalando.fahrschein;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import org.zalando.fahrschein.domain.Cursor;

@SuppressWarnings("serial")
public class CursorCommitException extends IOException {
    private final int statusCode;
    private final Cursor cursor;
    private final String subscriptionId;

    public CursorCommitException(int statusCode, Cursor cursor, String subscriptionId, IOException cause) {
        super(formatMessage(statusCode, cursor, subscriptionId, Optional.empty()), cause);
        this.statusCode = statusCode;
        this.cursor = cursor;
        this.subscriptionId = subscriptionId;
    }

    public CursorCommitException(int statusCode, Cursor cursor, String subscriptionId, String responseBody) {
        super(formatMessage(statusCode, cursor, subscriptionId, Optional.of(responseBody)));
        this.statusCode = statusCode;
        this.cursor = cursor;
        this.subscriptionId = subscriptionId;
    }

    private static String formatMessage(int statusCode, Cursor cursor, String subscriptionId, Optional<String> responseBody) {
        String msg;
        switch (statusCode) {
            case 422:
                msg = String.format(
                    Locale.ENGLISH,
                    "Cursor for subscription [%s] and event type [%s] in partition [%s] with offset [%s] failed to commit because of status code 422 (Unprocessable Entity). " +
                    "This likely means that the processing time of the batch exceeded the timeout (defaults to 60 seconds). " + 
                    "In such case, you may want to investigate the slowness in the processing, and/or reduce the batch size.", 
                    nullSafe(subscriptionId), nullSafe(cursor.getEventType()), cursor.getPartition(), cursor.getOffset()) +
                    responseBody.map(s -> String.format(Locale.ENGLISH, " Response body: [%s]", s)).orElse("");
                break;
            default: 
                msg = String.format(Locale.ENGLISH, "Unexpected status code [%s] for subscription [%s] to event [%s].",
                                    statusCode, subscriptionId, nullSafe(cursor.getEventType())) +
                    responseBody.map(s -> String.format(Locale.ENGLISH, " Response body: [%s]", s)).orElse("");
                break;
        }
        return msg;
    }

    private static String nullSafe(String s) {
        return Optional.ofNullable(s).orElse("");
    }

    public Cursor getCursor() {
        return cursor;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
