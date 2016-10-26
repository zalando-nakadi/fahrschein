package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "data_type", visible = true)
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
