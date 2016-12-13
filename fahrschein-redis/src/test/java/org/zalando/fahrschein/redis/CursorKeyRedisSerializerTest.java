package org.zalando.fahrschein.redis;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class CursorKeyRedisSerializerTest {
    private static final List<RedisCursorKey> CURSORS_KEYS = asList(
            cursorKey("Super Consumer", "bla.blup-1234.yeah", "my_partition"),
            cursorKey("consumer1", "eventtype1", "kdsakjsd"),
            cursorKey("consumer_with_underscore", "event_type_with_underscore", "sth_with_underscore"),
            cursorKey("consumer_with_dash", "event_type_with_dash", "sth_with_dash")
    );

    private CursorKeyRedisSerializer serializer = new CursorKeyRedisSerializer();


    @Test
    public void serializeDeserializeRoundTrip() {
        for (RedisCursorKey cursorKey : CURSORS_KEYS) {
            final byte[] bytes = serializer.serialize(cursorKey);

            assertThat(bytes, notNullValue());
            assertThat(bytes.length, greaterThan(0));

            final RedisCursorKey actualCursor = serializer.deserialize(bytes);

            assertThat("Could not serialize and deserialize cursor key " + cursorKey.toString(),
                    actualCursor, IsEqual.equalTo(cursorKey));
        }
    }

    @Test
    public void canDeserializeNullValue() {
        assertThat(serializer.deserialize(null), is(nullValue()));
    }

    private static RedisCursorKey cursorKey(final String consumerName, final String eventTypeName, final String partition) {
        return new RedisCursorKey(consumerName, eventTypeName, partition);
    }


}