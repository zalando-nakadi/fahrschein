package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.hobsoft.hamcrest.compose.ComposeMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.fahrschein.NoMetricsCollector.NO_METRICS_COLLECTOR;

public class NakadiReaderTest {

    private static final String EVENT_NAME = "some-event";
    private final URI uri = java.net.URI.create("http://example.com/events");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CursorManager cursorManager = mock(CursorManager.class);
    private final ErrorHandler errorHandler = DefaultErrorHandler.INSTANCE;
    private final ClientHttpRequestFactory clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);

    @SuppressWarnings("unchecked")
    private final Listener<SomeEvent> listener = (Listener<SomeEvent>)mock(Listener.class);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        when(listener.isActive()).thenReturn(true);
    }

    public static class SomeEvent {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @Test
    public void shouldNotRetryInitialConnection() throws IOException {
        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenThrow(new IOException("Initial connection failed"));

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();

        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(IOException.class);
        expectedException.expectMessage(equalTo("Initial connection failed"));

        nakadiReader.run();
    }

    @Test
    public void shouldNotReconnectWithoutBackoff() throws IOException, InterruptedException, BackoffException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        when(response.getBody()).thenReturn(emptyInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature("retries", BackoffException::getRetries, equalTo(0)));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Stream was closed")));

        nakadiReader.runInternal();
    }

    @Test
    public void shouldHandleBrokenInput() throws IOException, InterruptedException, BackoffException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"".getBytes("utf-8"));
        when(response.getBody()).thenReturn(initialInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(0)));
        expectedException.expectCause(instanceOf(JsonProcessingException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Unexpected end-of-input")));

        nakadiReader.runInternal();
    }

    @Test
    public void shouldHandleBrokenInputInEvents() throws IOException, InterruptedException, BackoffException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"123\",\"offset\":\"456\"},\"events\":[{\"id\":".getBytes("utf-8"));
        when(response.getBody()).thenReturn(initialInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(0)));
        expectedException.expectCause(instanceOf(JsonProcessingException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Unexpected end-of-input")));

        nakadiReader.runInternal();
    }

    @Test
    public void shouldRetryConnectionOnEof() throws IOException, InterruptedException, BackoffException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"0\",\"offset\":\"0\"}}".getBytes("utf-8"));
        when(response.getBody()).thenReturn(initialInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response).thenThrow(new IOException("Reconnection failed"));

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final ExponentialBackoffStrategy backoffStrategy = new ExponentialBackoffStrategy(1, 1, 2, 1);
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(1)));
        expectedException.expectCause(instanceOf(IOException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Reconnection failed")));

        nakadiReader.runInternal();
    }

    @Test
    public void shouldRetryConnectionMultipleTimesOnEof() throws IOException, InterruptedException, BackoffException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"0\",\"offset\":\"0\"}}".getBytes("utf-8"));
        when(response.getBody()).thenReturn(initialInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response).thenThrow(new IOException("Reconnection failed"));

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final ExponentialBackoffStrategy backoffStrategy = new ExponentialBackoffStrategy(1, 1, 2, 4);
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(4)));
        expectedException.expectCause(instanceOf(IOException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Reconnection failed")));

        nakadiReader.runInternal();
    }

    @Test
    public void shouldRetryConnectionAfterExceptionDuringReconnection() throws IOException, InterruptedException, BackoffException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"0\",\"offset\":\"0\"}}".getBytes("utf-8"));
        when(response.getBody()).thenReturn(initialInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response).thenThrow(new IOException("Reconnection failed")).thenReturn(response).thenThrow(new IOException("Reconnection failed on second attempt"));

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final ExponentialBackoffStrategy backoffStrategy = new ExponentialBackoffStrategy(1, 1, 2, 4);
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(4)));
        expectedException.expectCause(instanceOf(IOException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Reconnection failed on second attempt")));

        nakadiReader.runInternal();
    }

    static class InterruptibleInputStream extends InputStream {
        private final InputStream delegate;

        InterruptibleInputStream(InputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            if (Thread.currentThread().isInterrupted()) {
                return -1;
            } else {
                return delegate.read();
            }
        }
    }

    @Test
    public void shouldBeInterruptible() throws IOException, InterruptedException, BackoffException, ExecutionException, TimeoutException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final InputStream endlessInputStream = new SequenceInputStream(new Enumeration<InputStream>() {
            @Override
            public boolean hasMoreElements() {
                return !Thread.currentThread().isInterrupted();
            }

            @Override
            public InputStream nextElement() {
                try {
                    return new InterruptibleInputStream(new ByteArrayInputStream("{\"cursor\":{\"partition\":\"0\",\"offset\":\"0\"}}".getBytes("utf-8")));
                } catch (UnsupportedEncodingException e) {
                    throw new UncheckedIOException(e);
                }
            }
        });
        when(response.getBody()).thenReturn(endlessInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        //expectedException.expect(InterruptedException.class);

        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        final Future<?> future = scheduledExecutorService.submit(() -> {
            final Thread currentThread = Thread.currentThread();
            scheduledExecutorService.schedule(currentThread::interrupt, 100, TimeUnit.MILLISECONDS);
            nakadiReader.unchecked().run();
        });

        Assert.assertNull("Thread should have completed normally", future.get());
    }


    @Test(timeout = 2000)
    public void shouldBeInterruptibleWhenReadingFromSocket() throws IOException, InterruptedException, BackoffException, ExecutionException, TimeoutException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        final ServerSocket serverSocket = new ServerSocket(0, 0, loopbackAddress);
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                try (final Socket socket = serverSocket.accept()) {
                    try (OutputStream out = socket.getOutputStream()) {
                        while (true) {
                            out.write("{\"cursor\":{\"partition\":\"0\",\"offset\":\"0\"}}\n".getBytes("utf-8"));
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        final int localPort = serverSocket.getLocalPort();
        final Socket socket = new Socket(loopbackAddress, localPort);
        socket.setSoTimeout(1000);
        final InputStream inputStream = socket.getInputStream();
        when(response.getBody()).thenReturn(inputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final BackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        final Future<?> future = scheduledExecutorService.submit(() -> {
            final Thread currentThread = Thread.currentThread();
            scheduledExecutorService.schedule(currentThread::interrupt, 100, TimeUnit.MILLISECONDS);
            nakadiReader.unchecked().run();
        });

        Assert.assertNull("Thread should have completed normally", future.get(500, TimeUnit.MILLISECONDS));
    }

    @Test
    public void shouldReturnInsteadOfReconnectOnInterruption() throws IOException, InterruptedException, BackoffException, ExecutionException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"0\",\"offset\":\"0\"}}".getBytes("utf-8"));
        when(response.getBody()).thenAnswer(invocation -> {
            Thread.currentThread().interrupt();
            return initialInputStream;
        });

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        final Future<?> future = Executors.newCachedThreadPool().submit(nakadiReader.unchecked());
        Assert.assertNull("Thread should have completed normally", future.get());
    }

    @Test
    public void shouldReturnOnInterruptionDuringReconnection() throws IOException, InterruptedException, BackoffException, ExecutionException {
        final ClientHttpResponse initialResponse = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"0\",\"offset\":\"0\"}}".getBytes("utf-8"));
        when(initialResponse.getBody()).thenReturn(initialInputStream);

        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        final ClientHttpResponse emptyResponse = mock(ClientHttpResponse.class);
        when(emptyResponse.getBody()).thenReturn(emptyInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(initialResponse).thenAnswer(invocation -> {
            Thread.currentThread().interrupt();
            throw new IOException("Reconnection failed");
        });

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final ExponentialBackoffStrategy backoffStrategy = new ExponentialBackoffStrategy(1, 1, 2, 4);
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        final Future<?> future = Executors.newCachedThreadPool().submit(nakadiReader.unchecked());
        Assert.assertNull("Thread should have completed normally", future.get());
    }

    @Test
    public void shouldProcessEventsAndCommitCursor() throws IOException, InterruptedException, BackoffException, EventAlreadyProcessedException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        String input = "{\"cursor\":{\"event_type\":\""+EVENT_NAME+"\",\"partition\":\"123\",\"offset\":\"456\"},\"events\":[{\"id\":\"789\"}]}";
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream(input.getBytes("utf-8"));
        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        when(response.getBody()).thenReturn(initialInputStream, emptyInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        // cannot use expectedException since there should be assertions afterwards
        try {
            nakadiReader.runInternal();
            fail("Expected IOException on reconnect");
        } catch (BackoffException e) {
            assertEquals(0, e.getRetries());
            assertEquals("Stream was closed", e.getCause().getMessage());

            {
                @SuppressWarnings("unchecked")
                final ArgumentCaptor<List<SomeEvent>> argumentCaptor = (ArgumentCaptor<List<SomeEvent>>)(Object)ArgumentCaptor.forClass(List.class);
                verify(listener).accept(argumentCaptor.capture());

                final List<SomeEvent> events = argumentCaptor.getValue();

                assertEquals(1, events.size());
                assertEquals("789", events.get(0).getId());
            }

            {
                final ArgumentCaptor<Cursor> argumentCaptor = ArgumentCaptor.forClass(Cursor.class);

                verify(cursorManager).onSuccess(ArgumentMatchers.eq(EVENT_NAME), argumentCaptor.capture());

                final Cursor cursor = argumentCaptor.getValue();
                assertEquals("123", cursor.getPartition());
                assertEquals("456", cursor.getOffset());
            }
        }
    }

    @Test
    public void shouldFailWithoutCursors() throws IOException, InterruptedException, BackoffException, EventAlreadyProcessedException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("{\"foo\":\"bar\"}".getBytes("utf-8"));
        when(response.getBody()).thenReturn(inputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(0)));
        expectedException.expectCause(instanceOf(IOException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Could not read cursor")));

        nakadiReader.runInternal();
    }

    @Test
    public void shouldIgnoreMetadataInEventBatch() throws IOException, InterruptedException, BackoffException, EventAlreadyProcessedException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"123\",\"offset\":\"456\"},\"events\":[{\"id\":\"789\"}],\"metadata\":{\"foo\":\"bar\"}}".getBytes("utf-8"));
        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        when(response.getBody()).thenReturn(initialInputStream, emptyInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(0)));
        expectedException.expectCause(instanceOf(IOException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Stream was closed")));

        nakadiReader.runInternal();
    }

    @Test
    public void shouldIgnoreAdditionalPropertiesInEventBatch() throws IOException, InterruptedException, BackoffException, EventAlreadyProcessedException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"123\",\"offset\":\"456\"},\"foo\":\"bar\",\"events\":[{\"id\":\"789\"}],\"metadata\":{\"foo\":\"bar\"}}".getBytes("utf-8"));
        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        when(response.getBody()).thenReturn(initialInputStream, emptyInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(0)));
        expectedException.expectCause(instanceOf(IOException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Stream was closed")));

        nakadiReader.runInternal();
    }

    @Test
    public void shouldIgnoreAdditionalPropertiesInEventBatchInAnyOrder() throws IOException, InterruptedException, BackoffException, EventAlreadyProcessedException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"foo\":\"bar\",\"cursor\":{\"partition\":\"123\",\"offset\":\"456\"},\"events\":[{\"id\":\"789\"}],\"baz\":123}".getBytes("utf-8"));
        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        when(response.getBody()).thenReturn(initialInputStream, emptyInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(0)));
        expectedException.expectCause(instanceOf(IOException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Stream was closed")));

        nakadiReader.runInternal();
    }

    @Test
    public void shouldSendCursorsForLockedPartitions() throws IOException {
        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenThrow(new IOException("Initial connection failed"));
        final HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();

        when(cursorManager.getCursors(EVENT_NAME)).thenReturn(asList(new Cursor("0", "0"), new Cursor("1", "10"), new Cursor("2", "20"), new Cursor("3", "30")));

        final Lock lock = new Lock(EVENT_NAME, "test", asList(new Partition("0", "0", "100"), new Partition("1", "0", "100")));
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.of(lock), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        expectedException.expect(IOException.class);
        expectedException.expectMessage(equalTo("Initial connection failed"));

        nakadiReader.run();

        assertEquals(singletonList("[{\"partition\":\"0\",\"offset\":\"0\"},{\"partition\":\"1\",\"offset\":\"10\"}"), headers.get("X-Nakadi-Cursors"));
    }

    @Test
    public void shouldCloseOnIOException() throws IOException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final byte[] bytes = "{\"cursor\":{\"partition\":\"123\",\"offset\":\"456\"},\"events\":[{\"id\":\"789\"}]}".getBytes("utf-8");
        when(response.getBody()).thenReturn(new ByteArrayInputStream(bytes), new ByteArrayInputStream(bytes), new ByteArrayInputStream(bytes), new ByteArrayInputStream(bytes));

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);
        final HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final ExponentialBackoffStrategy backoffStrategy = new ExponentialBackoffStrategy().withMaxRetries(2);

        final Listener<SomeEvent> listener = events -> {
            throw new FileNotFoundException("from listener");
        };

        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        try {
            nakadiReader.run();
        } catch (FileNotFoundException e) {
            assertEquals("from listener", e.getMessage());

            verify(request, times(3)).execute();
            verify(response, times(3)).close();
        }
    }

    @Test
    public void shouldCloseOnRuntimeException() throws IOException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"123\",\"offset\":\"456\"},\"foo\":\"bar\",\"events\":[{\"id\":\"789\"}],\"metadata\":{\"foo\":\"bar\"}}".getBytes("utf-8"));
        when(response.getBody()).thenReturn(inputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);
        final HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();

        final Listener<SomeEvent> listener = events -> {
            throw new RuntimeException("from listener");
        };

        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        try {
            nakadiReader.run();
        } catch (RuntimeException e) {
            assertEquals("from listener", e.getMessage());

            verify(request).execute();
            verify(response).close();
        }
    }

    @Test
    public void shouldCloseOnInactiveListener() throws IOException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"123\",\"offset\":\"456\"},\"foo\":\"bar\",\"events\":[{\"id\":\"789\"}],\"metadata\":{\"foo\":\"bar\"}}".getBytes("utf-8"));
        when(response.getBody()).thenReturn(inputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);
        final HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();

        final Listener<SomeEvent> listener = new Listener<SomeEvent>() {
            @Override
            public void accept(List<SomeEvent> events) throws IOException, EventAlreadyProcessedException {

            }

            @Override
            public boolean isActive() {
                return false;
            }
        };

        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, Collections.singleton(EVENT_NAME), Optional.empty(), Optional.empty(), SomeEvent.class, listener, errorHandler, NO_METRICS_COLLECTOR);

        nakadiReader.run();

        verify(request).execute();
        verify(response).close();
    }

}
