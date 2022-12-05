package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Authorization;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;

import java.io.IOException;
import java.util.List;

public interface SubscriptionBuilder {
    SubscriptionBuilder readFromBegin();

    SubscriptionBuilder readFromEnd();

    SubscriptionBuilder readFromCursors(List<Cursor> initialCursors);

    SubscriptionBuilder withConsumerGroup(String consumerGroup);

    SubscriptionBuilder withAuthorization(Authorization authorization);

    Subscription subscribe() throws IOException;
}
