package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.hobsoft.hamcrest.compose.ComposeMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.fahrschein.cursormanager.CursorManager;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    public void shouldNotRetryInitialConnection() throws IOException, InterruptedException, BackoffException {
        final ClientHttpRequest request = mock(ClientHttpRequest.class);
        when(request.execute()).thenThrow(new IOException("Initial connection failed"));

        when(clientHttpRequestFactory.createRequest(uri, HttpMethod.GET)).thenReturn(request);

        final NoBackoffStrategy backoffStrategy = new NoBackoffStrategy();

        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener);

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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature("retries", BackoffException::getRetries, equalTo(0)));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Stream was closed")));

        nakadiReader.runInternal(-1, TimeUnit.MILLISECONDS);
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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(0)));
        expectedException.expectCause(instanceOf(JsonProcessingException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Unexpected end-of-input")));

        nakadiReader.runInternal(-1, TimeUnit.MILLISECONDS);
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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(1)));
        expectedException.expectCause(instanceOf(IOException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Reconnection failed")));

        nakadiReader.runInternal(-1, TimeUnit.MILLISECONDS);
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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener);

        expectedException.expect(BackoffException.class);
        expectedException.expect(ComposeMatchers.hasFeature(BackoffException::getRetries, equalTo(4)));
        expectedException.expectCause(instanceOf(IOException.class));
        expectedException.expectCause(ComposeMatchers.hasFeature("message", Exception::getMessage, Matchers.containsString("Reconnection failed")));

        nakadiReader.runInternal(-1, TimeUnit.MILLISECONDS);
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
        final NakadiReader<SomeEvent> nakadiReader = new NakadiReader<>(uri, clientHttpRequestFactory, backoffStrategy, cursorManager, objectMapper, EVENT_NAME, Optional.<Subscription>empty(), SomeEvent.class, listener);

        // cannot use expectedException since there should be assertions afterwards
        try {
            nakadiReader.runInternal(-1, TimeUnit.MILLISECONDS);
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

                verify(cursorManager).onSuccess(eq(EVENT_NAME), argumentCaptor.capture());

                final Cursor cursor = argumentCaptor.getValue();
                assertEquals("123", cursor.getPartition());
                assertEquals("456", cursor.getOffset());
            }
        }
    }

}
