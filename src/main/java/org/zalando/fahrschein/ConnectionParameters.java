package org.zalando.fahrschein;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConnectionParameters {
    public static final int DEFAULT_READ_TIMEOUT = 2 * 60 * 1000;
    public static final int DEFAULT_CONNECT_TIMEOUT = 1000;

    private final int connectTimeout;
    private final int readTimeout;
    private Map<String, String> headers;
    private final int errorCount;

    public ConnectionParameters(int connectTimeout, int readTimeout, Map<String, String> headers, int errorCount) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.errorCount = errorCount;
    }

    public ConnectionParameters(int connectTimeout, int readTimeout, Map<String, String> headers) {
        this(connectTimeout, readTimeout, headers, 0);
    }

    public ConnectionParameters() {
        this(DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, Collections.emptyMap());
    }

    public ConnectionParameters withConnectTimeout(int connectTimeout) {
        return new ConnectionParameters(connectTimeout, readTimeout, headers, errorCount);
    }

    public ConnectionParameters withReadTimeout(int readTimeout) {
        return new ConnectionParameters(connectTimeout, readTimeout, headers, errorCount);
    }

    public ConnectionParameters withHeaders(Map<String, String> headers) {
        final Map<String, String> newHeaders = new LinkedHashMap<>(headers);
        newHeaders.putAll(headers);
        return new ConnectionParameters(connectTimeout, readTimeout, newHeaders, errorCount);
    }
    public ConnectionParameters withHeader(final String header, @Nullable final String value) {
        final Map<String, String> newHeaders = new LinkedHashMap<>(headers);
        if (value == null) {
            newHeaders.remove(header);
        } else {
            newHeaders.put(header, value);
        }
        return new ConnectionParameters(connectTimeout, readTimeout, newHeaders, errorCount);
    }

    public ConnectionParameters withAuthorization(String authorizationHeader) {
        return withHeader("Authorization", authorizationHeader);
    }

    public ConnectionParameters withErrorCount(int errorCount) {
        return new ConnectionParameters(connectTimeout, readTimeout, headers, errorCount);
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getErrorCount() {
        return errorCount;
    }
}
