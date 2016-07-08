package org.zalando.fahrschein.cursormanager.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.Base64Utils;
import org.zalando.fahrschein.domain.Cursor;

import java.io.IOException;

class CursorRedisSerializer implements RedisSerializer<Cursor> {

    @Override
    public byte[] serialize(final Cursor cursor) throws SerializationException {
        try {
            return new StringToByteArrayWriter()
                    .write(cursor.getPartition())
                    .write(cursor.getOffset())
                    .serialize();
        } catch (IOException e) {
            throw new SerializationException("Could not serialize cursor " + cursor.toString(), e);
        }
    }

    @Override
    public Cursor deserialize(final byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }

        try {
            final StringFromByteArrayReader reader = new StringFromByteArrayReader(bytes);
            return new Cursor(reader.readNextString(), reader.readNextString());
        } catch (IOException e) {
            final String dataAsBase64 = Base64Utils.encodeToString(bytes);
            throw new SerializationException("Could not deserialize redis data to cursor. Data: " + dataAsBase64, e);
        }
    }
}
