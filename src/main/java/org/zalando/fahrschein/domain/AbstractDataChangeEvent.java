package org.zalando.fahrschein.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractDataChangeEvent<T> implements DataChangeEvent<T> {

    public class FieldNames {
        public static final String DATA_TYPE = "data_type";
        public static final String DATA_OP = "data_op";
        public static final String METADATA = "metadata";
        public static final String DATA = "data";
    }

    private final Metadata metadata;

    @JsonProperty(FieldNames.DATA_TYPE)
    private final String dataType;

    @JsonProperty(FieldNames.DATA_OP)
    private final DataOperation dataOp;

    private final T data;

    protected AbstractDataChangeEvent(@JsonProperty(FieldNames.METADATA) Metadata metadata, @JsonProperty(FieldNames.DATA_TYPE) String dataType, @JsonProperty(FieldNames.DATA_OP) DataOperation dataOp, @JsonProperty(FieldNames.DATA) T data) {
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
