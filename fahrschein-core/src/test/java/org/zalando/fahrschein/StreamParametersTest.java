package org.zalando.fahrschein;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;

public class StreamParametersTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

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

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("stream_timeout is lower than batch_flush_timeout.");

        final StreamParameters streamParameters = new StreamParameters()
                .withBatchFlushTimeout(100)
                .withStreamTimeout(50);
    }

    @Test
    public void streamParametersWithStreamLimitFailure() throws IllegalArgumentException {

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("streamLimit is lower than batch_limit.");

        final StreamParameters streamParameters = new StreamParameters()
                .withBatchLimit(20)
                .withStreamLimit(10);
    }

    @Test
    public void streamParametersWithBatchLimitZero() throws IllegalArgumentException {

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("batch_limit can't be lower than 1.");

        final StreamParameters streamParameters = new StreamParameters()
                .withBatchLimit(0)
                .withStreamLimit(10);
    }

}