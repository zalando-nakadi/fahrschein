package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

public enum Position {
    BEGIN("begin"), END("end");

    private final String value;

    Position(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}