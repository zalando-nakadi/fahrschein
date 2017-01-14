package org.zalando.fahrschein.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

class CursorKeyRedisSerializer implements RedisSerializer<RedisCursorKey> {

    public static final Codec CODEC = new Codec();

    @Override
    public byte[] serialize(final RedisCursorKey cursorKey) throws SerializationException {
        return CODEC.serialize(cursorKey.getConsumerName(), cursorKey.getEventType(), cursorKey.getPartition());
    }

    @Override
    public RedisCursorKey deserialize(final byte[] bytes) throws SerializationException {
        final String[] values = CODEC.deserialize(bytes);

        if (values == null) {
            return null;
        }

        return new RedisCursorKey(values[0], values[1], values[2]);
    }
}
