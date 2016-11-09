package org.zalando.fahrschein.domain;

public interface DataChangeEvent<T> extends Event {
    @Override
    Metadata getMetadata();

    String getDataType();

    DataOperation getDataOp();

    T getData();
}
