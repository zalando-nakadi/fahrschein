package org.zalando.fahrschein.http.api;

import com.github.luben.zstd.ZstdOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public enum ContentEncoding {
    IDENTITY("identity"), GZIP("gzip"), ZSTD("zstd");

    private final String value;

    ContentEncoding(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public OutputStream wrap(OutputStream out) throws IOException {
        switch (this) {
            case GZIP:
                return new GZIPOutputStream(out);
            case ZSTD:
                return new ZstdOutputStream(out);
            default:
                return out;
        }

    }
}
