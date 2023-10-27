package org.zalando.fahrschein;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StreamParametersTest {

    @Test
    public void createsDefaultQueryParam() {
        final StreamParameters streamParameters = new StreamParameters();
        final String queryString = streamParameters.toQueryString();
        assertEquals("", queryString);
    }

    @Test
    public void createsAValidQueryParamString() {
        final StreamParameters streamParameters = new StreamParameters()
                .withBatchLimit(102)
                .withStreamLimit(103)
                .withBatchFlushTimeout(104)
                .withStreamTimeout(105)
                .withStreamKeepAliveLimit(106)
                .withMaxUncommittedEvents(107)
                .withCommitTimeout(60)
                .withBatchTimespan(10);

        final String queryString = streamParameters.toQueryString();
        final String[] queryParamStrings = queryString.split("&");

        assertThat(queryParamStrings, arrayWithSize(8));

        assertThat(queryParamStrings, arrayContainingInAnyOrder(
                "batch_limit=102",
                "stream_limit=103",
                "batch_flush_timeout=104",
                "stream_timeout=105",
                "stream_keep_alive_limit=106",
                "max_uncommitted_events=107",
                "commit_timeout=60",
                "batch_timespan=10"
        ));

        assertEquals(Optional.of(102), streamParameters.getBatchLimit());
        assertEquals(Optional.of(103), streamParameters.getStreamLimit());
        assertEquals(Optional.of(104), streamParameters.getBatchFlushTimeout());
        assertEquals(Optional.of(105), streamParameters.getStreamTimeout());
        assertEquals(Optional.of(106), streamParameters.getStreamKeepAliveLimit());
        assertEquals(Optional.of(107), streamParameters.getMaxUncommittedEvents());
        assertEquals(Optional.of(60), streamParameters.getCommitTimeout());
        assertEquals(Optional.of(10), streamParameters.getBatchTimespan());
    }

}