package org.zalando.fahrschein.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Meter;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DropwizardMetricsCollectorTest {

    @Test
    public void shouldCollectMetrics() {
        String namespace = "test.";
        MetricRegistry metricRegistry = new MetricRegistry();
        DropwizardMetricsCollector c = new DropwizardMetricsCollector(metricRegistry, namespace);
        assertEquals(5, metricRegistry.getMeters().entrySet().size());

        c.markErrorWhileConsuming();
        Map<String, Meter> meters = metricRegistry.getMeters();
        assertEquals(1, meters.get("test.errorsWhileConsuming").getCount());


    }

}
