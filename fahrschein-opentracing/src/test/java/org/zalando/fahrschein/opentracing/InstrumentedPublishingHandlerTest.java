package org.zalando.fahrschein.opentracing;

import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.fahrschein.http.api.Response;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class InstrumentedPublishingHandlerTest {

    private final MockTracer tracer = new MockTracer();

    private InstrumentedPublishingHandler instrumentedPublishingHandler;

    @Mock
    private Response response;

    @BeforeEach
    public void init() {
        instrumentedPublishingHandler = new InstrumentedPublishingHandler(tracer);
    }

    @AfterEach
    public void after() {
        tracer.reset();
    }


    @Test
    public void successfulPublish() {
        //When
        instrumentedPublishingHandler.onPublish("test_event", Arrays.asList("ev1", "ev2"));
        instrumentedPublishingHandler.afterPublish();

        // then
        assertEquals(1, tracer.finishedSpans().size(), "finished spans");
        assertEquals("send_test_event", tracer.finishedSpans().get(0).operationName(), "send_test_event");
        assertEquals("{messaging.system=Nakadi, messaging.destination=test_event, messaging.message_payload_size=1-10, messaging.destination_kind=topic, span.kind=producer}", tracer.finishedSpans().get(0).tags().toString(), "send_test_event tags");
    }

    @Test
    public void failedPublish() throws IOException {
        // given
        Throwable somethingWentWrong = new IOException("something went wrong");

        // when
        instrumentedPublishingHandler.onPublish("test_event", Arrays.asList("ev1", "ev2"));
        instrumentedPublishingHandler.onError(Arrays.asList("ev1", "ev2"), somethingWentWrong);
        instrumentedPublishingHandler.afterPublish();

        // then
        assertEquals(1, tracer.finishedSpans().size(), "finished spans");
        assertEquals("send_test_event", tracer.finishedSpans().get(0).operationName(), "send_test_event");
        assertEquals(true, tracer.finishedSpans().get(0).tags().get(Tags.ERROR.getKey()), "send_test_event error=true");
    }

    @Test
    public void testBucketing() {
        assertEquals("0", instrumentedPublishingHandler.sizeBucket(0));
        assertEquals("1-10", instrumentedPublishingHandler.sizeBucket(1));
        assertEquals("1-10", instrumentedPublishingHandler.sizeBucket(2));
        assertEquals("1-10", instrumentedPublishingHandler.sizeBucket(10));
        assertEquals("11-20", instrumentedPublishingHandler.sizeBucket(11));
        assertEquals("101-110", instrumentedPublishingHandler.sizeBucket(104));
        assertEquals("2101-2110", instrumentedPublishingHandler.sizeBucket(2101));
        assertEquals("2101-2110", instrumentedPublishingHandler.sizeBucket(2105));
    }

}