package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractDataChangeEvent<T> implements DataChangeEvent<T> {
    private final Metadata metadata;
    private final String dataType;
    private final DataOperation dataOp;
    private final T data;

    protected AbstractDataChangeEvent(@JsonProperty("metadata") Metadata metadata, @JsonProperty("data_type") String dataType, @JsonProperty("data_op") DataOperation dataOp, @JsonProperty("data") T data) {
        this.metadata = metadata;
        this.dataType = dataType;
        this.dataOp = dataOp;
        this.data = data;
    }

    @Override
    public final Metadata getMetadata() {
        return metadata;
    }

    @Override
    public final String getDataType() {
        return dataType;
    }

    @Override
    public final DataOperation getDataOp() {
        return dataOp;
    }

    @Override
    public final T getData() {
        return data;
    }
}
