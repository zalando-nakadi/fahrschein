package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.zalando.spring.boot.fahrschein.config.TimeSpan;

import java.util.concurrent.TimeUnit;

public class BackoffConfig {

    private Boolean enabled;

    private TimeSpan initialDelay;

    private TimeSpan maxDelay;

    private Double backoffFactor;

    private Integer maxRetries;

    private JitterConfig jitter;

    public BackoffConfig() {
    }

    public static BackoffConfig defaultBackoffConfig() {
        BackoffConfig c = new BackoffConfig();
        c.setEnabled(Boolean.FALSE);
        c.setInitialDelay(TimeSpan.of(500, TimeUnit.MILLISECONDS));
        c.setMaxDelay(TimeSpan.of(10, TimeUnit.MINUTES));
        c.setBackoffFactor(1.5);
        c.setMaxRetries(-1);
        c.setJitter(new JitterConfig(Boolean.FALSE, JitterType.EQUAL));
        return c;
    }

    public void mergeFromDefaults(BackoffConfig defaultBackoffConfig) {
        if (defaultBackoffConfig.getEnabled()) {
            setEnabled(Boolean.TRUE);
        }
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public TimeSpan getInitialDelay() {
        return this.initialDelay;
    }

    public TimeSpan getMaxDelay() {
        return this.maxDelay;
    }

    public Double getBackoffFactor() {
        return this.backoffFactor;
    }

    public Integer getMaxRetries() {
        return this.maxRetries;
    }

    public JitterConfig getJitter() {
        return this.jitter;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setInitialDelay(TimeSpan initialDelay) {
        this.initialDelay = initialDelay;
    }

    public void setMaxDelay(TimeSpan maxDelay) {
        this.maxDelay = maxDelay;
    }

    public void setBackoffFactor(Double backoffFactor) {
        this.backoffFactor = backoffFactor;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setJitter(JitterConfig jitter) {
        this.jitter = jitter;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BackoffConfig)) return false;
        final BackoffConfig other = (BackoffConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$enabled = this.getEnabled();
        final Object other$enabled = other.getEnabled();
        if (this$enabled == null ? other$enabled != null : !this$enabled.equals(other$enabled)) return false;
        final Object this$initialDelay = this.getInitialDelay();
        final Object other$initialDelay = other.getInitialDelay();
        if (this$initialDelay == null ? other$initialDelay != null : !this$initialDelay.equals(other$initialDelay))
            return false;
        final Object this$maxDelay = this.getMaxDelay();
        final Object other$maxDelay = other.getMaxDelay();
        if (this$maxDelay == null ? other$maxDelay != null : !this$maxDelay.equals(other$maxDelay)) return false;
        final Object this$backoffFactor = this.getBackoffFactor();
        final Object other$backoffFactor = other.getBackoffFactor();
        if (this$backoffFactor == null ? other$backoffFactor != null : !this$backoffFactor.equals(other$backoffFactor))
            return false;
        final Object this$maxRetries = this.getMaxRetries();
        final Object other$maxRetries = other.getMaxRetries();
        if (this$maxRetries == null ? other$maxRetries != null : !this$maxRetries.equals(other$maxRetries))
            return false;
        final Object this$jitter = this.getJitter();
        final Object other$jitter = other.getJitter();
        if (this$jitter == null ? other$jitter != null : !this$jitter.equals(other$jitter)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BackoffConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $enabled = this.getEnabled();
        result = result * PRIME + ($enabled == null ? 43 : $enabled.hashCode());
        final Object $initialDelay = this.getInitialDelay();
        result = result * PRIME + ($initialDelay == null ? 43 : $initialDelay.hashCode());
        final Object $maxDelay = this.getMaxDelay();
        result = result * PRIME + ($maxDelay == null ? 43 : $maxDelay.hashCode());
        final Object $backoffFactor = this.getBackoffFactor();
        result = result * PRIME + ($backoffFactor == null ? 43 : $backoffFactor.hashCode());
        final Object $maxRetries = this.getMaxRetries();
        result = result * PRIME + ($maxRetries == null ? 43 : $maxRetries.hashCode());
        final Object $jitter = this.getJitter();
        result = result * PRIME + ($jitter == null ? 43 : $jitter.hashCode());
        return result;
    }

    public String toString() {
        return "BackoffConfig(enabled=" + this.getEnabled() + ", initialDelay=" + this.getInitialDelay() + ", maxDelay=" + this.getMaxDelay() + ", backoffFactor=" + this.getBackoffFactor() + ", maxRetries=" + this.getMaxRetries() + ", jitter=" + this.getJitter() + ")";
    }
}
