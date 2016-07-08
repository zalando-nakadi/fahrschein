package org.zalando.fahrschein.cursormanager.redis;

import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.zalando.fahrschein.domain.Cursor;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class CursorRedisSerializerTest {

    private static final List<Cursor> CURSORS = asList(
            cursor("my_partition", "12345"),
            cursor("kdsakjsd", "sljdsajkdsa"),
            cursor("sth_with_underscore", "offset_with_underscore"),
            cursor("sth-with-dash", "offset-with-dash")
    );

    private CursorRedisSerializer serializer = new CursorRedisSerializer();


    @Test
    public void serializeDeserializeRoundTrip() {
        for (Cursor cursor : CURSORS) {
            final byte[] bytes = serializer.serialize(cursor);

            assertThat(bytes, notNullValue());
            assertThat(bytes.length, greaterThan(0));

            final Cursor actualCursor = serializer.deserialize(bytes);

            assertThat("Could not serialize and deserialize cursor " + cursor.toString(),
                    actualCursor, IsEqual.equalTo(cursor));
        }
    }

    @Test
    public void canDeserializeNullValue() {
        assertThat(serializer.deserialize(null), is(nullValue()));
    }

    private static Cursor cursor(final String partition, final String offset) {
        return new Cursor(partition, offset);
    }

}