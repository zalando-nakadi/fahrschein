package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.zalando.fahrschein.NakadiReaderTest.SomeEvent;

import java.net.URI;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NakadiClientTest {
    public static final String FLOW_ID = "1234567890abcdef";
    private final URI uri = java.net.URI.create("http://example.com");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CursorManager cursorManager = mock(CursorManager.class);
    private final ClientHttpRequestFactory clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);
    private Listener<SomeEvent> listener = events -> { /* do nothing */ };
    private NakadiReaderFactory nakadiReaderFactory;
    private NakadiReader nakadiReader;

    @Before
    public void setup() {
        nakadiReaderFactory = mock(NakadiReaderFactory.class);
        nakadiReader = mock(NakadiReader.class);
        when(nakadiReaderFactory.nakadiReader(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(nakadiReader);
    }

    @Test
    public void canStartWithoutFlowIdProvider() throws Exception {
        final NakadiClient nakadiClient = new NakadiClient(uri, clientHttpRequestFactory, new NoBackoffStrategy(),
                objectMapper, cursorManager);
        nakadiClient.setNakadiReaderFactory(nakadiReaderFactory);

        nakadiClient.listen("some.event", SomeEvent.class, listener);
    }

    @Test
    public void canStartWithFlowIdProvider() throws Exception {
        final NakadiClient nakadiClient = new NakadiClient(uri, clientHttpRequestFactory, new NoBackoffStrategy(),
                objectMapper, cursorManager);
        nakadiClient.setNakadiReaderFactory(nakadiReaderFactory);
        nakadiClient.setFlowIdProvider(() -> FLOW_ID);


        nakadiClient.listen("some.event", SomeEvent.class, listener);

        final ArgumentCaptor<Optional<String>> flowIdArgument = forClass((Class<Optional<String>>) (Class) Optional.class);
        verify(nakadiReader).run(anyLong(), any(), flowIdArgument.capture());

        assertThat(flowIdArgument.getValue(), equalTo(Optional.of(FLOW_ID)));
    }

}