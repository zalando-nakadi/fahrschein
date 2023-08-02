package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

public class ThreadConfig {

    private int listenerPoolSize = 1;

    public ThreadConfig() {
    }

    public int getListenerPoolSize() {
        return this.listenerPoolSize;
    }

    public void setListenerPoolSize(int listenerPoolSize) {
        this.listenerPoolSize = listenerPoolSize;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ThreadConfig)) return false;
        final ThreadConfig other = (ThreadConfig) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getListenerPoolSize() != other.getListenerPoolSize()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ThreadConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getListenerPoolSize();
        return result;
    }

    public String toString() {
        return "ThreadConfig(listenerPoolSize=" + this.getListenerPoolSize() + ")";
    }
}
