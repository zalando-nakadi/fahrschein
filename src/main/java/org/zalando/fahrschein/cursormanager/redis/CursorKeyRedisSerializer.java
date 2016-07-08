package org.zalando.fahrschein.cursormanager.redis;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

class CursorKeyRedisSerializer implements RedisSerializer<RedisCursorKey> {

    //public static final char DELIMITER = '|';
    public static final char DELIMITER = 0;

    @Override
    public byte[] serialize(final RedisCursorKey redisCursorKey) throws SerializationException {
        final String combinedKey = redisCursorKey.getConsumerName() + DELIMITER + redisCursorKey.getEventType() + DELIMITER + redisCursorKey.getPartition();
        return combinedKey.getBytes(RedisCursorManager.UTF8);
    }

    @Override
    public RedisCursorKey deserialize(final byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }

        final String combinedKey = new String(bytes, RedisCursorManager.UTF8);
        final String[] splittedKey = StringUtils.split(combinedKey, DELIMITER);
        return new RedisCursorKey(splittedKey[0], splittedKey[1], splittedKey[2]);
    }
}
