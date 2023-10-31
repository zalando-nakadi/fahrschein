package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Immutable
public final class Metadata {
    private final String eventType;
    private final String eid;
    private final String partition;
    private final String version;
    private final String publishedBy;
    private final OffsetDateTime occurredAt;
    private final OffsetDateTime receivedAt;
    private final String flowId;
    private final Map<String, String> spanCtx;

    private final String partitionCompactionKey;

    @JsonCreator
    @Deprecated
    private Metadata(@JsonProperty("event_type") String eventType, @JsonProperty("eid") String eid, @JsonProperty("occurred_at") String occurredAt, @JsonProperty("partition") String partition, @JsonProperty("version") String version, @JsonProperty("published_by") String publishedBy, @JsonProperty("received_at") String receivedAt, @JsonProperty("flow_id") String flowId, @JsonProperty("span_ctx") Map<String, String> spanCtx, @JsonProperty("partition_compaction_key") String partitionCompactionKey) {
        this(eventType, eid, occurredAt == null ? null : OffsetDateTime.parse(occurredAt), partition, version, publishedBy, receivedAt == null ? null : OffsetDateTime.parse(receivedAt), flowId, spanCtx, partitionCompactionKey);
    }

    public Metadata(String eventType, String eid, OffsetDateTime occurredAt, String partition, String version, String publishedBy, OffsetDateTime receivedAt, String flowId) {
        this(eventType, eid, occurredAt, partition, version, publishedBy, receivedAt, flowId, null,null);
    }

    public Metadata(String eventType, String eid, OffsetDateTime occurredAt, String partition, String version, String publishedBy, OffsetDateTime receivedAt, String flowId, Map<String, String> spanCtx,String partitionCompactionKey) {
        this.eventType = eventType;
        this.eid = eid;
        this.occurredAt = occurredAt;
        this.partition = partition;
        this.version = version;
        this.publishedBy = publishedBy;
        this.receivedAt = receivedAt;
        this.flowId = flowId;
        this.spanCtx = spanCtx == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(spanCtx));
        this.partitionCompactionKey = partitionCompactionKey;
    }

    public Metadata(String eid, OffsetDateTime occurredAt) {
        this(null, eid, occurredAt, null, null,  null, null, null, null,null);
    }

    public Metadata(String eid, OffsetDateTime occurredAt, String flowId, Map<String, String> spanCtx) {
    	this(null, eid, occurredAt, null, null, null, null, flowId, spanCtx,null);
    }
    
    public String getEventType() {
        return eventType;
    }

    public String getEid() {
        return eid;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getPartition() { return partition; }

    public String getVersion() { return version; }

    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }

    public String getFlowId() {
        return flowId;
    }

    public Map<String, String> getSpanCtx() {
        return spanCtx;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    public String getPartitionCompactionKey() {
        return partitionCompactionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata metadata = (Metadata) o;
        return Objects.equals(eventType, metadata.eventType) && Objects.equals(eid, metadata.eid) && Objects.equals(partition, metadata.partition) && Objects.equals(version, metadata.version) && Objects.equals(publishedBy, metadata.publishedBy) && Objects.equals(occurredAt, metadata.occurredAt) && Objects.equals(receivedAt, metadata.receivedAt) && Objects.equals(flowId, metadata.flowId) && Objects.equals(spanCtx, metadata.spanCtx) && Objects.equals(partitionCompactionKey, metadata.partitionCompactionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                eventType,
                eid,
                partition,
                version,
                publishedBy,
                occurredAt,
                receivedAt,
                flowId,
                spanCtx,
                partitionCompactionKey
        );
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "eventType='" + eventType + '\'' +
                ", eid='" + eid + '\'' +
                ", occurredAt=" + occurredAt +
                ", partition='" + partition + '\'' +
                ", version='" + version + '\'' +
                ", publishedBy='" + publishedBy + '\'' +
                ", receivedAt=" + receivedAt +
                ", flowId='" + flowId + '\'' +
                ", partitionCompactionKey='" + partitionCompactionKey + '\'' +
                ", spanCtx=" + spanCtx.toString() +
                '}';
    }
}
