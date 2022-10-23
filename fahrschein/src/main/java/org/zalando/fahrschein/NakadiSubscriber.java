package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Subscription;

import java.io.IOException;
import java.util.Set;

public interface NakadiSubscriber {
    SubscriptionBuilder subscription(String applicationName, String eventName) throws IOException;

    SubscriptionBuilder subscription(String applicationName, Set<String> eventNames) throws IOException;

    StreamBuilder.SubscriptionStreamBuilder stream(Subscription subscription);
}
