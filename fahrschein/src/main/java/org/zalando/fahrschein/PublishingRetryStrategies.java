package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.BatchItemResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public enum PublishingRetryStrategies implements PublishingRetryStrategy {
    ALL {
        @Override
        public <T> List<T> getEventsForRetry(final EventPersistenceException ex) {
            return (List<T>) ex.getInputEvents();
        }

    }, FAILED_ONLY {
        @Override
        public <T> List<T> getEventsForRetry(final EventPersistenceException ex) {
            Preconditions.checkArgument(ex.getInputEvents().size() == ex.getResponses().length, "Invalid number of responses from Nakadi. It has to match size of the input batch");
            var x = (List<T>) IntStream.range(0, ex.getResponses().length)
                    .filter(i -> BatchItemResponse.PublishingStatus.FAILED == ex.getResponses()[i].getPublishingStatus())
                    .mapToObj(i -> ex.getInputEvents().get(i)).toList();
            return x;
        }
    },
    ALL_FROM_FIRST_FAILURE {
        @Override
        public <T> List<T> getEventsForRetry(final EventPersistenceException ex) {
            Preconditions.checkArgument(ex.getInputEvents().size() == ex.getResponses().length, "Invalid number of responses from Nakadi. It has to match size of the input batch");
            var firstFailure = IntStream.range(0, ex.getResponses().length)
                    .filter(i -> BatchItemResponse.PublishingStatus.FAILED == ex.getResponses()[i].getPublishingStatus())
                    .findFirst();

            if (firstFailure.isEmpty()) {
                return Collections.emptyList();
            } else {
                return (List<T>) ex.getInputEvents().subList(firstFailure.getAsInt(), ex.getInputEvents().size());
            }
        }
    },
    NONE {
        @Override
        public <T> List<T> getEventsForRetry(final EventPersistenceException ex) {
            return Collections.emptyList();
        }
    }

}
