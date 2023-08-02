package org.zalando.spring.boot.fahrschein.nakadi.config;

import org.zalando.fahrschein.NakadiClient;
import org.zalando.spring.boot.fahrschein.nakadi.NakadiPublisher;

import java.io.IOException;
import java.util.List;

class DefaultNakadiPublisher implements NakadiPublisher {
    private final NakadiClient nakadiClient;

    DefaultNakadiPublisher(NakadiClient nakadiClient) {
        this.nakadiClient = nakadiClient;
    }

    @Override
    public <Type> void publish(String eventName, List<Type> events) throws IOException {
        nakadiClient.publish(eventName, events);
    }

}
