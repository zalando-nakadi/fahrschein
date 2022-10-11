package org.zalando.fahrschein;

import java.io.IOException;
import java.util.Set;

public interface NakadiSubscriber {
    SubscriptionBuilder subscription(String applicationName, String eventName) throws IOException;

    SubscriptionBuilder subscription(String applicationName, Set<String> eventNames) throws IOException;
}
