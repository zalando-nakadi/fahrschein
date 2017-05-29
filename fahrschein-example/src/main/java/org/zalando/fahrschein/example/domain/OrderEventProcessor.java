package org.zalando.fahrschein.example.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderEventProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(OrderEventProcessor.class);

    public void processPaymentAccepted(final OrderPaymentAcceptedEvent event) {
        LOG.info("[{}] OrderPaymentAcceptedEvent [{}] [{}]", event.getMetadata().getOccurredAt(), event.getOrderNumber(), event.getPaymentMethod());
    }

    public void processOrderCreated(final OrderCreatedEvent event) {
        LOG.info("[{}] OrderCreatedEvent [{}] [{}]", event.getMetadata().getOccurredAt(), event.getOrderNumber(), event.getCustomerNumber());
    }
}
