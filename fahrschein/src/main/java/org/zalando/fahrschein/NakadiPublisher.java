package org.zalando.fahrschein;

import java.io.IOException;
import java.util.List;

public interface NakadiPublisher {
    <T> void publish(String eventName, List<T> events) throws EventPublishingException, IOException;
}
