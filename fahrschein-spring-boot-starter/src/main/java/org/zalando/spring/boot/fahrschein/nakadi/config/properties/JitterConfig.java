package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

public class JitterConfig {

    private Boolean enabled = Boolean.FALSE;

    private JitterType type = JitterType.EQUAL;

    public JitterConfig(Boolean enabled, JitterType type) {
        this.enabled = enabled;
        this.type = type;
    }

    public JitterConfig() {
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public JitterType getType() {
        return this.type;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setType(JitterType type) {
        this.type = type;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof JitterConfig)) return false;
        final JitterConfig other = (JitterConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$enabled = this.getEnabled();
        final Object other$enabled = other.getEnabled();
        if (this$enabled == null ? other$enabled != null : !this$enabled.equals(other$enabled)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof JitterConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $enabled = this.getEnabled();
        result = result * PRIME + ($enabled == null ? 43 : $enabled.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        return result;
    }

    public String toString() {
        return "JitterConfig(enabled=" + this.getEnabled() + ", type=" + this.getType() + ")";
    }
}
