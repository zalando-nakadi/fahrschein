package org.zalando.fahrschein;

import org.junit.jupiter.api.Test;
import org.zalando.fahrschein.domain.BatchItemResponse;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PublishingRetryStrategiesTest {

    private EnrichedEventPersistenceException allFailed = new EnrichedEventPersistenceException(List.of("a","b", "c"), new EventPersistenceException(new BatchItemResponse[]{
        new BatchItemResponse("a", BatchItemResponse.PublishingStatus.FAILED, BatchItemResponse.Step.PUBLISHING, ""),
            new BatchItemResponse("b", BatchItemResponse.PublishingStatus.FAILED, BatchItemResponse.Step.PUBLISHING, ""),
            new BatchItemResponse("c", BatchItemResponse.PublishingStatus.FAILED, BatchItemResponse.Step.PUBLISHING, ""),
    }));

    private EnrichedEventPersistenceException someFailed = new EnrichedEventPersistenceException(List.of("a","b","c"), new EventPersistenceException(new BatchItemResponse[]{
            new BatchItemResponse("a", BatchItemResponse.PublishingStatus.SUBMITTED, BatchItemResponse.Step.PUBLISHING, ""),
            new BatchItemResponse("a", BatchItemResponse.PublishingStatus.FAILED, BatchItemResponse.Step.PUBLISHING, ""),
            new BatchItemResponse("b", BatchItemResponse.PublishingStatus.SUBMITTED, BatchItemResponse.Step.PUBLISHING, ""),
    }));

    private EnrichedEventPersistenceException noneFailed = new EnrichedEventPersistenceException(List.of("a","b","c"), new EventPersistenceException(new BatchItemResponse[]{
            new BatchItemResponse("a", BatchItemResponse.PublishingStatus.SUBMITTED, BatchItemResponse.Step.PUBLISHING, ""),
            new BatchItemResponse("a", BatchItemResponse.PublishingStatus.SUBMITTED, BatchItemResponse.Step.PUBLISHING, ""),
            new BatchItemResponse("b", BatchItemResponse.PublishingStatus.SUBMITTED, BatchItemResponse.Step.PUBLISHING, ""),
    }));


    @Test
    public void testNoRetry() {
        var SUT = PublishingRetryStrategies.NONE;
        assertEquals(Collections.emptyList(), SUT.getEventsForRetry(allFailed));
        assertEquals(Collections.emptyList(), SUT.getEventsForRetry(someFailed));
        assertEquals(Collections.emptyList(), SUT.getEventsForRetry(noneFailed));
    }

    @Test
    public void testFullRetry() {
        var SUT = PublishingRetryStrategies.ALL;
        assertEquals(allFailed.getInputEvents(), SUT.getEventsForRetry(allFailed));
        assertEquals(someFailed.getInputEvents(), SUT.getEventsForRetry(someFailed));
        assertEquals(noneFailed.getInputEvents(), SUT.getEventsForRetry(noneFailed));
    }

    @Test
    public void testFailedOnlyRetry() {
        var SUT = PublishingRetryStrategies.FAILED_ONLY;
        assertEquals(allFailed.getInputEvents(), SUT.getEventsForRetry(allFailed));
        assertEquals(someFailed.getInputEvents().subList(1,2), SUT.getEventsForRetry(someFailed));
        assertEquals(Collections.emptyList(), SUT.getEventsForRetry(noneFailed));
    }

    @Test
    public void testStartingWithFailedEventRetry() {
        var SUT = PublishingRetryStrategies.ALL_FROM_FIRST_FAILURE;
        assertEquals(allFailed.getInputEvents(), SUT.getEventsForRetry(allFailed));
        assertEquals(someFailed.getInputEvents().subList(1,3), SUT.getEventsForRetry(someFailed));
        assertEquals(Collections.emptyList(), SUT.getEventsForRetry(noneFailed));
    }
}
