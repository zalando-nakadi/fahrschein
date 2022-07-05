package org.zalando.fahrschein;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StreamParametersTest {

    @Test
    public void createsAValidQueryParamString() throws IllegalArgumentException {
        final StreamParameters streamParameters = new StreamParameters()
                .withBatchLimit(102)
                .withStreamLimit(103)
                .withBatchFlushTimeout(104)
                .withStreamTimeout(105)
                .withStreamKeepAliveLimit(106)
                .withMaxUncommittedEvents(107)
                .withCommitTimeout(60);

        final String queryString = streamParameters.toQueryString();
        final String[] queryParamStrings = queryString.split("&");

        assertThat(queryParamStrings, arrayWithSize(7));

        assertThat(queryParamStrings, arrayContainingInAnyOrder(
                "batch_limit=102",
                "stream_limit=103",
                "batch_flush_timeout=104",
                "stream_timeout=105",
                "stream_keep_alive_limit=106",
                "max_uncommitted_events=107",
                "commit_timeout=60"
        ));
    }

    @Test
    public void streamParametersWithStreamTimeoutFailure() throws IllegalArgumentException {


        IllegalArgumentException expectedException = assertThrows(IllegalArgumentException.class, () -> {
            final StreamParameters streamParameters = new StreamParameters()
                    .withBatchFlushTimeout(100)
                    .withStreamTimeout(50);
        });

        assertThat(expectedException.getMessage(), is("stream_timeout is lower than batch_flush_timeout."));
    }

    @Test
    public void streamParametersWithStreamLimitFailure() {

        IllegalArgumentException expectedException = assertThrows(IllegalArgumentException.class, () -> {
            new StreamParameters()
                    .withBatchLimit(20)
                    .withStreamLimit(10);
        });
        assertThat(expectedException.getMessage(), is("streamLimit is lower than batch_limit."));
    }

    @Test
    public void streamParametersWithBatchLimitZero() throws IllegalArgumentException {
        IllegalArgumentException expectedException = assertThrows(IllegalArgumentException.class, () -> {
            new StreamParameters()
                    .withBatchLimit(0)
                    .withStreamLimit(10);
        });

        assertThat(expectedException.getMessage(), is("batch_limit can't be lower than 1."));
    }

}