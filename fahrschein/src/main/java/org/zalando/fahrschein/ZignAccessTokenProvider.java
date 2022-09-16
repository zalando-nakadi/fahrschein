package org.zalando.fahrschein;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ZignAccessTokenProvider implements AccessTokenProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ZignAccessTokenProvider.class);
    private static final long CACHE_DURATION = 5 * 60 * 1000L;

    private final AtomicReference<Entry> token = new AtomicReference<>();

    static class Entry {
        final long timestamp;
        final String value;

        Entry(long timestamp, String value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

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
        final Process zign = new ProcessBuilder("zign", "token").start();
        try (final InputStream inputStream = zign.getInputStream()) {
            final String output = readAll(inputStream).trim();
            zign.waitFor(5, TimeUnit.SECONDS);
            if (zign.exitValue() != 0) {
                throw new IOException(String.format(Locale.ENGLISH, "zign failed with the exit code: %d", zign.exitValue()));
            }
            LOG.debug("Refreshed token from zign");
            return output;
        } catch (InterruptedException e) {
            throw new IOException("zign process took longer than 5 seconds to exit");
        }
    }

    private static Entry update(@Nullable Entry entry)  {
        final long now = System.currentTimeMillis();
        try {
            return entry == null || entry.timestamp < now - CACHE_DURATION ? new Entry(now, zign()) : entry;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getAccessToken() throws IOException {
        try {
            return token.updateAndGet(ZignAccessTokenProvider::update).value;
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

}
