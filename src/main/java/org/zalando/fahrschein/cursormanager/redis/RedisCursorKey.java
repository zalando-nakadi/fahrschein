package org.zalando.fahrschein.cursormanager.redis;

class RedisCursorKey {
    private final String consumerName;
    private final String eventType;
    private final String partition;

    RedisCursorKey(final String consumerName, final String eventType, final String partition) {
        this.consumerName = consumerName;
        this.eventType = eventType;
        this.partition = partition;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPartition() {
        return partition;
    }

    @Override
    public String toString() {
        return "RedisCursorKey{" +
                "consumerName='" + consumerName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", partition='" + partition + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RedisCursorKey that = (RedisCursorKey) o;

        if (consumerName != null ? !consumerName.equals(that.consumerName) : that.consumerName != null) return false;
        if (eventType != null ? !eventType.equals(that.eventType) : that.eventType != null) return false;
        return partition != null ? partition.equals(that.partition) : that.partition == null;

    }

    @Override
    public int hashCode() {
        int result = consumerName != null ? consumerName.hashCode() : 0;
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        result = 31 * result + (partition != null ? partition.hashCode() : 0);
        return result;
    }
}
