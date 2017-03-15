package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;
import org.zalando.fahrschein.domain.SubscriptionRequest;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.zalando.fahrschein.Preconditions.checkArgument;
import static org.zalando.fahrschein.Preconditions.checkState;

public class SubscriptionBuilder {
    private static final String DEFAULT_CONSUMER_GROUP = "default";

    private final NakadiClient nakadiClient;
    private final String applicationName;
    private final Set<String> eventNames;
    private final String consumerGroup;
    private final SubscriptionRequest.Position readFrom;
    @Nullable
    private final List<Cursor> initialCursors;

    SubscriptionBuilder(NakadiClient nakadiClient, String applicationName, Set<String> eventNames) {
        this(nakadiClient, applicationName, eventNames, DEFAULT_CONSUMER_GROUP, SubscriptionRequest.Position.END, null);
    }

    private SubscriptionBuilder(NakadiClient nakadiClient, String applicationName, Set<String> eventNames, String consumerGroup, SubscriptionRequest.Position readFrom, @Nullable List<Cursor> initialCursors) {
        this.nakadiClient = nakadiClient;
        this.applicationName = applicationName;
        this.eventNames = eventNames;
        this.consumerGroup = consumerGroup;
        this.readFrom = readFrom;
        this.initialCursors = initialCursors;
    }

    public SubscriptionBuilder readFromBegin() {
        checkState(initialCursors == null, "Initial cursors can not be specified when reading from 'begin'");
        return new SubscriptionBuilder(nakadiClient, applicationName, eventNames, consumerGroup, SubscriptionRequest.Position.BEGIN, null);
    }

    public SubscriptionBuilder readFromEnd() {
        checkState(initialCursors == null, "Initial cursors can not be specified when reading from 'end'");
        return new SubscriptionBuilder(nakadiClient, applicationName, eventNames, consumerGroup, SubscriptionRequest.Position.END, null);
    }

    public SubscriptionBuilder readFromCursors(List<Cursor> initialCursors) {
        checkArgument(initialCursors != null, "Initial cursors have to be specified");
        return new SubscriptionBuilder(nakadiClient, applicationName, eventNames, consumerGroup, SubscriptionRequest.Position.CURSORS, initialCursors);
    }

    public SubscriptionBuilder withConsumerGroup(String consumerGroup) {
        return new SubscriptionBuilder(nakadiClient, applicationName, eventNames, consumerGroup, readFrom, initialCursors);
    }

    public Subscription subscribe() throws IOException {
        return nakadiClient.subscribe(applicationName, eventNames, consumerGroup, readFrom, initialCursors);
    }

}
