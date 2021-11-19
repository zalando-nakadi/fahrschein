package org.zalando.fahrschein;

import java.io.IOException;
import java.util.Optional;

import org.zalando.fahrschein.domain.Cursor;

@SuppressWarnings("serial")
public class CursorOffsetCommitException extends IOException {
    private final int statusCode;
    private final Cursor cursor;
    private final String subscriptionId;

    public CursorOffsetCommitException(int statusCode, Cursor cursor, String subscriptionId, IOException cause) {
        super(formatMessage(statusCode, cursor, subscriptionId), cause);
        this.statusCode = statusCode;
        this.cursor = cursor;
        this.subscriptionId = subscriptionId;
    }

    public CursorOffsetCommitException(int statusCode, Cursor cursor, String subscriptionId) {
        super(formatMessage(statusCode, cursor, subscriptionId));
        this.statusCode = statusCode;
        this.cursor = cursor;
        this.subscriptionId = subscriptionId;
    }

    private static String formatMessage(int statusCode, Cursor cursor, String subscriptionId) {
        String msg;
        switch (statusCode) {
            case 422:
                msg = String.format(
                    "Cursor for subscription [%s] to event [%s] in partition [%s] with offset [%s] failed to commit because of error 422 (Unprocessable Entity). " +
                    "This likely means that the processing time of the batch exceeded the timeout of 60 seconds. " + 
                    "In such case, you may want to investigate the slowness in the processing, and/or reduce the batch size.", 
                    nullSafe(subscriptionId), nullSafe(cursor.getEventType()), cursor.getPartition(), cursor.getOffset());
                break;
            default: 
                msg = String.format("Unexpected status code [%s] for subscription [%s] to event [%s]", 
                                    statusCode, subscriptionId, nullSafe(cursor.getEventType()));
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
