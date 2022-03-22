package org.zalando.fahrschein.http.api;

public enum ContentEncoding {
    IDENTITY("identity"), GZIP("gzip");

    private final String encoding;

    ContentEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }
}
