package org.zalando.spring.boot.fahrschein.nakadi;

import java.io.IOException;
import java.util.List;

public interface NakadiPublisher {

    <Type> void publish(String eventName, List<Type> events) throws IOException;

}
