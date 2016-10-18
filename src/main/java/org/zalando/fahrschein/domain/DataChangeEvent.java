package org.zalando.fahrschein.domain;

public interface DataChangeEvent<T> extends Event {
    String getDataType();

    DataOperation getOperation();

    T getData();
}
