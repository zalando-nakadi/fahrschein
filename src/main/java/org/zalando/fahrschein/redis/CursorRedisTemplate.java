package org.zalando.fahrschein.redis;

import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.zalando.fahrschein.domain.Cursor;

final class CursorRedisTemplate extends RedisTemplate<RedisCursorKey, Cursor> {
    public CursorRedisTemplate() {
    }

    public CursorRedisTemplate(final RedisConnectionFactory connectionFactory) {
        this();
        setConnectionFactory(connectionFactory);

        setKeySerializer(new CursorKeyRedisSerializer());
        final CursorRedisSerializer valueSerializer = new CursorRedisSerializer();
        setDefaultSerializer(valueSerializer);
        setValueSerializer(valueSerializer);

        afterPropertiesSet();
    }
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        return new DefaultStringRedisConnection(connection);
    }
}
