package org.zalando.fahrschein.metrics.micrometer;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MicrometerMetricsCollectorTest {

    @Test
    public void shouldCollectMetrics() {
        String namespace = "test.";
        MockClock clock = new MockClock();
        MeterRegistry meterRegistry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, clock);
        MicrometerMetricsCollector c = new MicrometerMetricsCollector(meterRegistry, namespace);
        assertEquals(10, meterRegistry.getMeters().size());
        c.markErrorWhileConsuming();
        clock.addSeconds(1);
        assertEquals(1, fetchValue(meterRegistry, "test.errorsWhileConsuming").getValue());
        assertEquals(1, fetchValue(meterRegistry, "test.lastErrorHappened").getValue());

    }

    private Measurement fetchValue(MeterRegistry meterRegistry, String meterName) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().equals(meterName))
                .map(Meter::measure)
                .flatMap(measurements -> StreamSupport.stream(measurements.spliterator(), false))
                .limit(1).findFirst().get();
    }
}
