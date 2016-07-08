package org.zalando.fahrschein.cursormanager.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.zalando.fahrschein.cursormanager.CursorManager;
import org.zalando.fahrschein.domain.Cursor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RedisCursorManager implements CursorManager {
    private static final Logger LOG = LoggerFactory.getLogger(RedisCursorManager.class);
    public static final Charset UTF8 = Charset.forName("UTF-8");

    private final CursorRedisTemplate redisTemplate;
    private final String consumerName;

    public RedisCursorManager(final JedisConnectionFactory jedisConnectionFactory, final String consumerName) {
        this.redisTemplate = new CursorRedisTemplate(jedisConnectionFactory);
        this.consumerName = consumerName;
    }

    @Override
    public void onSuccess(final String eventName, final Cursor cursor) throws IOException {
        redisTemplate.opsForValue().set(cursorKey(eventName, cursor.getPartition()), cursor);
    }

    @Override
    public void onError(final String eventName, final Cursor cursor, final Throwable throwable) throws IOException {
        LOG.warn("Exception while processing events for [{}] on partition [{}] at offset [{}]. Don't update cursor.", eventName, cursor.getPartition(), cursor.getOffset(), throwable);
    }

    @Override
    public Collection<Cursor> getCursors(final String eventName) throws IOException {
        final RedisCursorKey pattern = new RedisCursorKey(consumerName, eventName, "*");

        final Set<RedisCursorKey> keys = redisTemplate.keys(pattern);
        final List<Cursor> cursors = new ArrayList<>(keys.size());
        for (RedisCursorKey key : keys) {
            final Cursor cursor = redisTemplate.opsForValue().get(key);
            cursors.add(cursor);
        }

        return cursors;
    }

    private RedisCursorKey cursorKey(final String eventName, final String partition) {
        return new RedisCursorKey(consumerName, eventName, partition);
    }

}
