package org.zalando.fahrschein.example.domain;

import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.EventProcessingException;
import org.zalando.fahrschein.example.RuntimeEventProcessingException;
import org.zalando.fahrschein.opentelemetry.InstrumentedEventListener;

public class MultiEventProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MultiEventProcessor.class);
    private final InstrumentedEventListener paymentWrapper;
    private final InstrumentedEventListener orderWrapper;

    public MultiEventProcessor(Tracer tracer) {
        this.paymentWrapper = new InstrumentedEventListener(tracer, "payment_accepted");
        this.orderWrapper = new InstrumentedEventListener(tracer, "payment_accepted");
    }

    public void process(final PaymentAcceptedEvent event) {
        paymentWrapper.accept(event, e -> {
            LOG.info("Processing PaymentAcceptedEvent [order:{}] [paymentMethod:{}]", e.getOrderNumber(), e.getPaymentMethod());
        });
    }

    public void process(final OrderCreatedEvent event) throws EventProcessingException {
        try {
            orderWrapper.accept(event, e -> {
                LOG.info("Processing OrderCreatedEvent [order:{}] [customerNumber:{}] [grandTotal:{}]", e.getOrderNumber(), e.getCustomerNumber(), e.getGrandTotal());
                if (Math.random() < 0.01) {
                    // anything can fail in here....
                    throw new RuntimeEventProcessingException("Failure to trigger a retry");
                }
            });
        } catch (RuntimeEventProcessingException e) {
            // and, if you want retry-semantics, you need to produce an exception that extends IOException
            throw new EventProcessingException(e);
        }

    }
}
