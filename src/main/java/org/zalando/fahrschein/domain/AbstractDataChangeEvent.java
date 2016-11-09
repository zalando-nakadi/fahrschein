package org.zalando.fahrschein.domain;

public abstract class AbstractDataChangeEvent<T> implements DataChangeEvent<T> {
    private final Metadata metadata;
    private final String dataType;
    private final DataOperation dataOp;
    private final T data;

    protected AbstractDataChangeEvent( Metadata metadata,  DataOperation dataOp, T data) {
        this.metadata = metadata;
        this.dataType = getDataType();
        this.dataOp = dataOp;
        this.data = data;
    }

    @Override
    public final Metadata getMetadata() {
        return metadata;
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
