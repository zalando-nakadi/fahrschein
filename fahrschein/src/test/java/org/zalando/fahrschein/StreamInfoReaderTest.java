package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StreamInfoReaderTest {

    private static final String EVENT_NAME = "some-event";
    private final URI uri = URI.create("http://example.com/events");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CursorManager cursorManager = mock(CursorManager.class);
    private final RequestFactory RequestFactory = mock(RequestFactory.class);

    @SuppressWarnings("unchecked")
    private final Listener<NakadiReaderTest.SomeEvent> listener = (Listener<NakadiReaderTest.SomeEvent>) mock(Listener.class);

    class LastResult<E> implements Answer {
        E lastResult = null;

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            lastResult = (E) invocation.callRealMethod();
            return lastResult;
        }
    }

    private void assertBackoffException(BackoffException expectedException, int retries, Class<? extends Exception> cause, String errorMessage) {
        assertAll("expectedException",
                () -> assertThat(expectedException.getRetries(), is(retries)),
                () -> assertThat(expectedException.getCause(), instanceOf(cause)),
                () -> assertThat(expectedException.getCause().getMessage(), containsString(errorMessage)));
    }

    // Tests

    @Test
    public void shouldExtractStreamInfoDebug() throws IOException {
        final Response response = mock(Response.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"0\",\"offset\":\"0\"},\"events\":[{\"id\":\"1\",\"foo\":\"bar\"},{}], \"info\":{\"debug\":\"DEBUG INFO\"}}".getBytes("utf-8"));
        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        when(response.getBody()).thenReturn(initialInputStream, emptyInputStream);

        final Request request = mock(Request.class);
        when(request.execute()).thenReturn(response);

        when(RequestFactory.createRequest(uri, "GET")).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();

        StreamInfoReader streamInfoReader = spy(StreamInfoReader.getDefault());
        LastResult<Optional<String>> lastResult = new LastResult<>();
        doAnswer(lastResult).when(streamInfoReader).readDebug(ArgumentMatchers.any());

        final List<String> ids = new ArrayList<>();

        final NakadiReader<String> nakadiReader = new NakadiReader<>(
                uri, RequestFactory, backoffStrategy, cursorManager,
                Collections.singleton(EVENT_NAME),
                Optional.empty(), Optional.empty(),
                new StringPropertyExtractingEventReader("id"), ids::addAll,
                DefaultBatchHandler.INSTANCE, NoMetricsCollector.NO_METRICS_COLLECTOR,
                streamInfoReader);

        BackoffException expectedException = assertThrows(BackoffException.class, () -> {
            nakadiReader.runInternal();
        });

        assertBackoffException(expectedException, 0, IOException.class, "Stream was closed");
        verify(streamInfoReader).readDebug(ArgumentMatchers.any());
        assertEquals(lastResult.lastResult.get(), "DEBUG INFO");
    }

    @Test
    public void shouldNotFailBecauseOfNonDebug() throws IOException {
        final Response response = mock(Response.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"0\",\"offset\":\"0\"},\"events\":[{\"id\":\"1\",\"foo\":\"bar\"},{}], \"info\":{\"a-different-key\":\"no-value\"}}".getBytes("utf-8"));
        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        when(response.getBody()).thenReturn(initialInputStream, emptyInputStream);

        final Request request = mock(Request.class);
        when(request.execute()).thenReturn(response);

        when(RequestFactory.createRequest(uri, "GET")).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();

        StreamInfoReader streamInfoReader = spy(StreamInfoReader.getDefault());
        LastResult<Optional<String>> lastResult = new LastResult<>();
        doAnswer(lastResult).when(streamInfoReader).readDebug(any());


        final List<String> ids = new ArrayList<>();

        final NakadiReader<String> nakadiReader = new NakadiReader<>(
                uri, RequestFactory, backoffStrategy, cursorManager,
                Collections.singleton(EVENT_NAME),
                Optional.empty(), Optional.empty(),
                new StringPropertyExtractingEventReader("id"), ids::addAll,
                DefaultBatchHandler.INSTANCE, NoMetricsCollector.NO_METRICS_COLLECTOR,
                streamInfoReader);

        BackoffException expectedException = assertThrows(BackoffException.class, () -> {
            nakadiReader.runInternal();
        });

        assertBackoffException(expectedException, 0, IOException.class, "Stream was closed");
        assertThat("There shouldn't be a debug value!", !lastResult.lastResult.isPresent());
    }

    @Test
    public void shouldNotFailBecauseOfEmptyInfo() throws IOException {
        final Response response = mock(Response.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"0\",\"offset\":\"0\"},\"events\":[{\"id\":\"1\",\"foo\":\"bar\"},{}], \"info\":{}}".getBytes("utf-8"));
        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        when(response.getBody()).thenReturn(initialInputStream, emptyInputStream);

        final Request request = mock(Request.class);
        when(request.execute()).thenReturn(response);

        when(RequestFactory.createRequest(uri, "GET")).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();

        StreamInfoReader streamInfoReader = spy(StreamInfoReader.getDefault());
        LastResult<Optional<String>> lastResult = new LastResult<>();
        doAnswer(lastResult).when(streamInfoReader).readDebug(any());

        final List<String> ids = new ArrayList<>();

        final NakadiReader<String> nakadiReader = new NakadiReader<>(
                uri, RequestFactory, backoffStrategy, cursorManager,
                Collections.singleton(EVENT_NAME),
                Optional.empty(), Optional.empty(),
                new StringPropertyExtractingEventReader("id"), ids::addAll,
                DefaultBatchHandler.INSTANCE, NoMetricsCollector.NO_METRICS_COLLECTOR,
                streamInfoReader);

        BackoffException expectedException = assertThrows(BackoffException.class, () -> {
            nakadiReader.runInternal();
        });

        assertBackoffException(expectedException, 0, IOException.class, "Stream was closed");
        assertThat("There shouldn't be a debug value!", !lastResult.lastResult.isPresent());

    }

    @Test
    public void shouldBeFineForNoInfo() throws IOException {
        final Response response = mock(Response.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"0\",\"offset\":\"0\"},\"events\":[{\"id\":\"1\",\"foo\":\"bar\"},{}]}".getBytes("utf-8"));
        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        when(response.getBody()).thenReturn(initialInputStream, emptyInputStream);

        final Request request = mock(Request.class);
        when(request.execute()).thenReturn(response);

        when(RequestFactory.createRequest(uri, "GET")).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();

        StreamInfoReader streamInfoReader = spy(StreamInfoReader.getDefault());

        final List<String> ids = new ArrayList<>();

        final NakadiReader<String> nakadiReader = new NakadiReader<>(
                uri, RequestFactory, backoffStrategy, cursorManager,
                Collections.singleton(EVENT_NAME),
                Optional.empty(), Optional.empty(),
                new StringPropertyExtractingEventReader("id"), ids::addAll,
                DefaultBatchHandler.INSTANCE, NoMetricsCollector.NO_METRICS_COLLECTOR,
                streamInfoReader);

        BackoffException expectedException = assertThrows(BackoffException.class, () -> {
            nakadiReader.runInternal();
        });

        assertBackoffException(expectedException, 0, IOException.class, "Stream was closed");
        verify(streamInfoReader, never()).readDebug(any());
    }


}
