package org.zalando.fahrschein;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ZignAccessTokenProvider implements AccessTokenProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ZignAccessTokenProvider.class);
    private static final long CACHE_DURATION = 5 * 60 * 1000L;

    static class Entry {
        final long timestamp;
        final String value;

        Entry(long timestamp, String value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    private final Object lock = new Object();
    private Entry token = null;

    private static String readAll(InputStream inputStream) throws IOException {
        try (final Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            final StringBuilder sb = new StringBuilder();
            final char[] buf = new char[1024];
            int len;
            while ((len = reader.read(buf)) != -1) {
                sb.append(buf, 0, len);
            }
            return sb.toString();
        }
    }

    private static String zign() throws IOException {
        LOG.info("Refreshing token from zign...");
        final Process zign = new ProcessBuilder("zign", "token", "uid").start();
        try (final InputStream inputStream = zign.getInputStream()) {
            return readAll(inputStream).trim();
        } finally {
            LOG.debug("Refreshed token from zign");
        }
    }

    @Override
    public String getAccessToken() throws IOException {
        long now = System.currentTimeMillis();
        Entry token = this.token;
        synchronized (lock) {
            if (token == null || token.timestamp < now - CACHE_DURATION) {
                token = new Entry(now, zign());
                this.token = token;
            }
        }

        return token.value;
    }

}
