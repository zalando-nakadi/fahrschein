package org.zalando.fahrschein;

import java.io.IOException;
import java.util.List;

public interface EventPublisher {
    <T> void publish(String eventName, List<T> events) throws EventPublishingException, IOException;
}
