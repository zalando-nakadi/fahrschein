package org.zalando.fahrschein.http.api;

public enum ContentEncoding {
    IDENTITY("identity"), GZIP("gzip");

    private final String value;

    ContentEncoding(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
