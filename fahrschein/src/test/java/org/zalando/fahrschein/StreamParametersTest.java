package org.zalando.fahrschein;

import org.junit.Test;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.Assert.assertThat;

public class StreamParametersTest {

    @Test
    public void createsAValidQueryParamString() throws StreamParametersException {
        final StreamParameters streamParameters = new StreamParameters()
                .withBatchLimit(102)
                .withStreamLimit(103)
                .withBatchFlushTimeout(104)
                .withStreamTimeout(105)
                .withStreamKeepAliveLimit(106)
                .withMaxUncommittedEvents(107);

        final String queryString = streamParameters.toQueryString();
        final String[] queryParamStrings = queryString.split("&");

        assertThat(queryParamStrings, arrayWithSize(6));

        assertThat(queryParamStrings, arrayContainingInAnyOrder(
                "batch_limit=102",
                "stream_limit=103",
                "batch_flush_timeout=104",
                "stream_timeout=105",
                "stream_keep_alive_limit=106",
                "max_uncommitted_events=107"
        ));
    }

    @Test(expected = StreamParametersException.class)
    public void streamParametersWithStreamTimeoutFailure() throws StreamParametersException {
        final StreamParameters streamParameters = new StreamParameters()
                .withBatchFlushTimeout(100)
                .withStreamTimeout(50);
    }

    @Test(expected = StreamParametersException.class)
    public void streamParametersWithStreamLimitFailure() throws StreamParametersException {
        final StreamParameters streamParameters = new StreamParameters()
                .withBatchLimit(20)
                .withStreamLimit(10);
    }

}