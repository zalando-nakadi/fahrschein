package org.zalando.fahrschein;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.hobsoft.hamcrest.compose.ComposeMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.salesorder.domain.DataChangeEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

import static org.zalando.fahrschein.metrics.NoMetricsCollector.NO_METRICS_COLLECTOR;

public class NakadiReaderTest {

    private static final String EVENT_NAME = "some-event";
    private final URI uri = java.net.URI.create("http://example.com/events");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CursorManager cursorManager = mock(CursorManager.class);
    private final ClientHttpRequestFactory clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);

    @SuppressWarnings("unchecked")
    private final Listener<SomeEvent> listener = (Listener<SomeEvent>)mock(Listener.class);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    public static class SomeEvent {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @Before
    public void before() {
    	objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }
    
    @Test
    public void shouldNotRetryInitialConnection() throws IOException {
        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenThrow(new IOException("Initial connection failed"));

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();

        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

        //expectedException.expect(InterruptedException.class);

        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        final Future<?> future = scheduledExecutorService.submit(() -> {
            final Thread currentThread = Thread.currentThread();
            scheduledExecutorService.schedule(currentThread::interrupt, 100, TimeUnit.MILLISECONDS);
            nakadiReader.unchecked().run();
        });

        Assert.assertNull("Thread should have completed normally", future.get());
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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

        final Future<?> future = Executors.newCachedThreadPool().submit(nakadiReader.unchecked());
        Assert.assertNull("Thread should have completed normally", future.get());
    }

    @Test
    public void shouldProcessEventsAndCommitCursor() throws IOException, InterruptedException, BackoffException, EventAlreadyProcessedException {
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"123\",\"offset\":\"456\"},\"events\":[{\"id\":\"789\"}]}".getBytes("utf-8"));
        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        when(response.getBody()).thenReturn(initialInputStream, emptyInputStream);

        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenReturn(response);

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
    public void shouldDeserializeParametricTypeFails() throws Exception {
    	final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"123\",\"offset\":\"456\"},\"events\":[{\"data_op\":\"C\",\"data_type\":\"sample-event\",\"data\":{\"id\":\"12345\"}}]}".getBytes("utf-8"));
        final ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        when(response.getBody()).thenReturn(initialInputStream, emptyInputStream);

        RecordingListener<DataChangeEvent> listener = new RecordingListener<>();
        
        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<DataChangeEvent> nakadiReader = new NakadiReader<DataChangeEvent>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), DataChangeEvent.class, listener, null);
        
        try {
        	nakadiReader.runInternal();
        	fail("Expected IOException on reconnect");
        } catch (BackoffException e) {
            List<DataChangeEvent> batch = listener.getLastResult();
            Assert.assertNotNull(batch);
            DataChangeEvent<?> event = batch.get(0);
            Assert.assertEquals("data-type", SomeEvent.class, event.getData().getClass());        	
        }

    }
        
    @Test
    public void shouldDeserializeParametricType() throws Exception {
    	final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ByteArrayInputStream initialInputStream = new ByteArrayInputStream("{\"cursor\":{\"partition\":\"123\",\"offset\":\"456\"},\"events\":[{\"data_op\":\"C\",\"data_type\":\"sample-event\",\"data\":{\"id\":\"12345\"}}]}".getBytes("utf-8"));

        
        JavaType eventType = objectMapper.getTypeFactory().constructParametricType(DataChangeEvent.class, SomeEvent.class);
        
        RecordingListener<DataChangeEvent> listener = new RecordingListener<>();
        
        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();
        final NakadiReader<DataChangeEvent> nakadiReader = new NakadiReader<DataChangeEvent>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), DataChangeEvent.class, eventType, listener, null);
        
        try {
        	nakadiReader.runInternal();
        	fail("Expected IOException on reconnect");
        } catch (BackoffException e) {
            List<DataChangeEvent> batch = listener.getLastResult();
            Assert.assertNotNull(batch);
            DataChangeEvent<?> event = batch.get(0);
            Assert.assertEquals("data-type", SomeEvent.class, event.getData().getClass());        	
        }

    }
    
    private class RecordingListener<T> implements Listener<T> {
    	
    	private List<List<T>> result = new ArrayList<>();
    	
    	public List<T> getLastResult() {
    		return result.size() == 0 ? null : result.get(0);
    	}
    	
    	@Override
    	public void accept(List<T> events) throws IOException, EventAlreadyProcessedException {
    		result.add(0, events);
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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener, NO_METRICS_COLLECTOR);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(0)));
        expectedException.expectCause(instanceOf(IOException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Stream was closed")));

        nakadiReader.runInternal();
    }

}