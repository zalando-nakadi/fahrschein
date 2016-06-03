package org.zalando.fahrschein.domain;

public interface DataChangeEvent extends Event {
    String getDataType();

    String getOperation();
}
