package org.zalando.fahrschein.metrics.dropwizard;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Meter;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DropwizardMetricsCollectorTest {

    @Test
    public void shouldCollectMetrics() {
        String namespace = "test";
        MetricRegistry metricRegistry = new MetricRegistry();
        DropwizardMetricsCollector c = new DropwizardMetricsCollector(metricRegistry, namespace);
        assertEquals(5, metricRegistry.getMeters().entrySet().size());
        assertEquals(5, metricRegistry.getGauges().entrySet().size());

        c.markErrorWhileConsuming();
        c.markMessageReceived();
        c.markEventsReceived(10);
        c.markEventsReceived(10);
        c.markReconnection();
        c.markMessageSuccessfullyProcessed();
        Map<String, Meter> meters = metricRegistry.getMeters();
        Map<String, Gauge> gauges = metricRegistry.getGauges();
        assertEquals(1, meters.get("test.errorsWhileConsuming").getCount());
        assertEquals(0, gauges.get("test.lastErrorHappened").getValue());
        assertEquals(20, meters.get("test.eventsReceived").getCount());
        assertEquals(1, meters.get("test.reconnections").getCount());
        assertEquals(1, meters.get("test.messagesSuccessfullyProcessed").getCount());
    }

}
