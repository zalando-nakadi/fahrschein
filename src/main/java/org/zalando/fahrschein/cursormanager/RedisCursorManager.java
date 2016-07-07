package org.zalando.fahrschein.cursormanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.Base64Utils;
import org.zalando.fahrschein.domain.Cursor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.function.Consumer;

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
        return redisTemplate.opsForList().getOperations().g
    }

    private RedisCursorKey cursorKey(final String eventName, final String partition) {
        return new RedisCursorKey(consumerName, eventName, partition);
    }

    private static class RedisCursorKey {
        private final String consumerName;
        private final String eventType;
        private final String partition;

        private RedisCursorKey(final String consumerName, final String eventType, final String partition) {
            this.consumerName = consumerName;
            this.eventType = eventType;
            this.partition = partition;
        }

        public String getConsumerName() {
            return consumerName;
        }

        public String getEventType() {
            return eventType;
        }

        public String getPartition() {
            return partition;
        }

        @Override
        public String toString() {
            return "RedisCursorKey{" +
                    "consumerName='" + consumerName + '\'' +
                    ", eventType='" + eventType + '\'' +
                    ", partition='" + partition + '\'' +
                    '}';
        }
    }

    private final class CursorRedisTemplate extends RedisTemplate<RedisCursorKey, Cursor> {
        public CursorRedisTemplate() {
        }

        public CursorRedisTemplate(final RedisConnectionFactory connectionFactory) {
            this();
            setConnectionFactory(connectionFactory);
            afterPropertiesSet();
        }
        protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
            return new DefaultStringRedisConnection(connection);
        }
    }

    private static class CursorRedisSerializer implements RedisSerializer<Cursor> {

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
            try {
                final StringFromByteArrayReader reader = new StringFromByteArrayReader(bytes);
                return new Cursor(reader.readNextString(), reader.readNextString());
            } catch (IOException e) {
                final String dataAsBase64 = Base64Utils.encodeToString(bytes);
                throw new SerializationException("Could not deserialize redis data to cursor. Data: " + dataAsBase64, e);
            }
        }
    }

    private static class CursorKeyRedisSerializer implements RedisSerializer<RedisCursorKey> {

        @Override
        public byte[] serialize(final RedisCursorKey redisCursorKey) throws SerializationException {
            try {
                return new StringToByteArrayWriter()
                        .write(redisCursorKey.getConsumerName())
                        .write(redisCursorKey.getEventType())
                        .write(redisCursorKey.getPartition())
                        .serialize();
            } catch (IOException e) {
                throw new SerializationException("Could not serialize cursor key " + redisCursorKey.toString(), e);
            }
        }

        @Override
        public RedisCursorKey deserialize(final byte[] bytes) throws SerializationException {
            try {
                final StringFromByteArrayReader reader = new StringFromByteArrayReader(bytes);
                return new RedisCursorKey(reader.readNextString(), reader.readNextString(), reader.readNextString());
            } catch (IOException e) {
                final String dataAsBase64 = Base64Utils.encodeToString(bytes);
                throw new SerializationException("Could not deserialize redis data to cursor. Data: " + dataAsBase64, e);
            }
        }
    }

    private static final class StringToByteArrayWriter {
        private final DataOutputStream dataOutputStream;
        private final ByteArrayOutputStream baos;

        private StringToByteArrayWriter() {
            baos = new ByteArrayOutputStream();
            dataOutputStream = new DataOutputStream(baos);
        }

        public StringToByteArrayWriter write(final String s) throws IOException {
            final byte[] bytes = s.getBytes(UTF8);
            dataOutputStream.writeInt(bytes.length);
            dataOutputStream.write(bytes);
            return this;
        }

        public byte[] serialize() {
            return baos.toByteArray();
        }
    }

    private static final class StringFromByteArrayReader {
        private final DataInputStream dataInputStream;

        private StringFromByteArrayReader(final byte[] bytes) {
            dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes));
        }

        public String readNextString() throws IOException {
            final int nextStringLength = dataInputStream.readInt();
            final byte[] bytes = new byte[nextStringLength];
            dataInputStream.read(bytes);
            return new String(bytes, UTF8);
        }

        public StringFromByteArrayReader readString(final Consumer<String> consumer) throws IOException {
            final String s = readNextString();
            consumer.accept(s);
            return this;
        }
    }
}
