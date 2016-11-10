package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface DataChangeEvent<T> extends Event {
    @Override
    @JsonProperty("metadata")
    Metadata getMetadata();

    @JsonProperty("data_type")
    String getDataType();

    @JsonProperty("data_op")
    DataOperation getDataOp();

    @JsonProperty("data")
    T getData();
}
