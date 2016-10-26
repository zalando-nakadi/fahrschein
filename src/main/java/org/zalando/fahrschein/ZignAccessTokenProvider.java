package org.zalando.fahrschein;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ZignAccessTokenProvider implements AccessTokenProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ZignAccessTokenProvider.class);

    private LoadingCache<String, String> zignCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(final String key) throws Exception {
                    return zign();
                }
            });


    private static String zign() throws IOException {
        LOG.info("Refreshing token from zign...");
        final Process zign = new ProcessBuilder("zign", "token", "uid").start();
        try (final InputStream inputStream = zign.getInputStream()) {
            return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8)).trim();
        } finally {
            LOG.debug("Refreshed token from zign");
        }
    }

    @Override
    public String getAccessToken() throws IOException {
        try {
            return zignCache.get("");
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            Throwables.propagateIfInstanceOf(cause, IOException.class);
            throw Throwables.propagate(cause);
        }
    }

}
