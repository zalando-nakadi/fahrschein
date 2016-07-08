package org.zalando.fahrschein.cursormanager.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.zalando.fahrschein.domain.Cursor;

class CursorRedisSerializer implements RedisSerializer<Cursor> {

    public static final Codec CODEC = new Codec();

    @Override
    public byte[] serialize(final Cursor cursor) throws SerializationException {
        return CODEC.serialize(cursor.getPartition(), cursor.getOffset());
    }

    @Override
    public Cursor deserialize(final byte[] bytes) throws SerializationException {
        final String[] values = CODEC.deserialize(bytes);

        if (values == null) {
            return null;
        }

        return new Cursor(values[0], values[1]);
    }
}
