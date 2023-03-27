package org.zalando.fahrschein.metrics.micrometer;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.zalando.fahrschein.metrics.micrometer.MicrometerMetricsCollector;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MicrometerMetricsCollectorTest {

    @Test
    public void shouldCollectMetrics() {
        String namespace = "test.";
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        MicrometerMetricsCollector c = new MicrometerMetricsCollector(meterRegistry, namespace);
        assertEquals(5, meterRegistry.getMeters().size());

        c.markErrorWhileConsuming();
        assertTrue(matchValue(meterRegistry, "test.errorsWhileConsuming", 1));
    }

    private boolean matchValue(MeterRegistry meterRegistry, String meterName, double value) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().equals(meterName))
                .map(Meter::measure)
                .flatMap(measurements -> StreamSupport.stream(measurements.spliterator(), false))
                .limit(1)
                .anyMatch(measurement -> measurement.getValue() == value);
    }
}
