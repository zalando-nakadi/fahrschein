package org.zalando.spring.boot.fahrschein.nakadi;

import io.micrometer.core.instrument.MeterRegistry;

public interface MeterRegistryAware {

    public void setMeterRegistry(MeterRegistry provider);

}
