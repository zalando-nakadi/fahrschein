package org.zalando.fahrschein.http.api;

import com.github.luben.zstd.ZstdOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

public enum ContentEncoding {
    IDENTITY("identity"), GZIP("gzip"), ZSTD("zstd");

    private final String value;

    ContentEncoding(String value) {
        this.value = value;
    }

    private static final Set<String> HTTP_METHODS_WITH_COMPRESSION_SUPPORT = new HashSet<>(Arrays.asList("POST"));

    public boolean isSupported(String httpMethod) {
        return HTTP_METHODS_WITH_COMPRESSION_SUPPORT.contains(httpMethod);
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
            case IDENTITY:
                return out;
            default:
                throw new UnsupportedOperationException(String.format("No output stream-wrapping defined for ContentEncoding: %s", out));
        }

    }
}