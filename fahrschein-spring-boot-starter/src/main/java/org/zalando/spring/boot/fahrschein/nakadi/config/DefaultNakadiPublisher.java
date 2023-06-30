package org.zalando.spring.boot.fahrschein.nakadi.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.spring.boot.fahrschein.nakadi.NakadiPublisher;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DefaultNakadiPublisher implements NakadiPublisher {
    private final NakadiClient nakadiClient;

    @Override
    public <Type> void publish(String eventName, List<Type> events) throws IOException {
        nakadiClient.publish(eventName, events);
    }

}
