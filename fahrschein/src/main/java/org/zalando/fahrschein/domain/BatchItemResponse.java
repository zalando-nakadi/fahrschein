package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public final class BatchItemResponse {

    public enum PublishingStatus {
        SUBMITTED("submitted"), ABORTED("aborted"), FAILED("failed");

        private final String value;

        PublishingStatus(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    public enum Step {
        NONE("none"),
        VALIDATING("validating"),
        PARTITIONING("partitioning"),
        ENRICHING("enriching"),
        PUBLISHING("publishing");

        private final String value;

        Step(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    private final String eid;
    private final PublishingStatus publishingStatus;
    private final Step step;
    private final String detail;

    @JsonCreator
    public BatchItemResponse(@JsonProperty("eid") String eid, @JsonProperty("publishing_status") PublishingStatus publishingStatus, @JsonProperty("step") Step step, @JsonProperty("detail") String detail) {
        this.eid = eid;
        this.publishingStatus = publishingStatus;
        this.step = step;
        this.detail = detail;
    }

    /**
     * @return the event id
     */
    public String getEid() {
        return eid;
    }

    public PublishingStatus getPublishingStatus() {
        return publishingStatus;
    }

    public Step getStep() {
        return step;
    }

    public String getDetail() {
        return detail;
    }
}
