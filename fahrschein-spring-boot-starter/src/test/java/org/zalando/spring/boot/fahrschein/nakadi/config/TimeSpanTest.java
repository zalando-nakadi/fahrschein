package org.zalando.spring.boot.fahrschein.nakadi.config;

import org.junit.jupiter.api.Test;
import org.zalando.spring.boot.fahrschein.config.TimeSpan;

import static java.util.concurrent.TimeUnit.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.*;

public class TimeSpanTest {

    @Test
    public void testEqualsHashCode() {
        assertThat(TimeSpan.of(10, MINUTES)).isEqualTo(TimeSpan.of(600, SECONDS));
        assertThat(TimeSpan.of(10, MINUTES)).isNotEqualTo(TimeSpan.of(601, SECONDS));
        assertThat(TimeSpan.of(10, MINUTES).hashCode()).isEqualTo(TimeSpan.of(600, SECONDS).hashCode());
    }

    @Test
    public void testConversion() {
        assertThat(TimeSpan.of(10, MINUTES).to(SECONDS)).isEqualTo(600);
    }

    @Test
    public void testToString() {
        assertThat(TimeSpan.of(10, MINUTES).toString()).isEqualTo("10 minutes");
    }

    @Test
    public void testParsing() {
        assertThat(new TimeSpan("5 minutes")).isEqualTo(TimeSpan.of(5, MINUTES));
        assertThat(new TimeSpan("")).isEqualTo(TimeSpan.of(0, MINUTES));
    }

    @Test
    public void testSecondsFallback() {
        assertThat(new TimeSpan(60)).isEqualTo(TimeSpan.of(60, SECONDS));
    }

}
