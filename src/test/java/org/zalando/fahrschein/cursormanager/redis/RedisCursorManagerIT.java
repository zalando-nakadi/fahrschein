package org.zalando.fahrschein.cursormanager.redis;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.zalando.fahrschein.cursormanager.CursorManager;
import org.zalando.fahrschein.domain.Cursor;
import redis.clients.jedis.JedisShardInfo;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class RedisCursorManagerIT {

    public static final String EVENT_TYPE_NAME = generateUniqueEventType();

    @Test
    @Ignore("Meant for local testing. You need a running redis cluster on localhost.")
    public void connectToRedisAndUseCursorManager() throws IOException {

        final JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setUsePool(true);
        jedisConnectionFactory.setShardInfo(new JedisShardInfo("localhost", 6379));
        final CursorManager cursorManager = new RedisCursorManager(jedisConnectionFactory, "fahrschein_redis_test");

        Collection<Cursor> cursors;

        // Precondition

        cursors = cursorManager.getCursors(EVENT_TYPE_NAME);

        assertThat("Precondition failed. Redis is not empty for event type " + EVENT_TYPE_NAME, cursors, empty());

        // First round - initial cursors

        final Cursor cursor1 = new Cursor("partition1", "101");
        final Cursor cursor2 = new Cursor("partition2", "202");
        final Cursor cursor3 = new Cursor("partition3", "303");

        cursorManager.onSuccess(EVENT_TYPE_NAME, cursor1);
        cursorManager.onSuccess(EVENT_TYPE_NAME, cursor2);
        cursorManager.onSuccess(EVENT_TYPE_NAME, cursor3);

        assertThat(cursorManager.getCursors(EVENT_TYPE_NAME), containsInAnyOrder(cursor1, cursor2, cursor3));

        // Second round - update cursors

        final Cursor cursor4 = new Cursor("partition1", "102");
        final Cursor cursor5 = new Cursor("partition3", "304");

        cursorManager.onSuccess(EVENT_TYPE_NAME, cursor4);
        cursorManager.onSuccess(EVENT_TYPE_NAME, cursor5);

        assertThat(cursorManager.getCursors(EVENT_TYPE_NAME), containsInAnyOrder(cursor4, cursor2, cursor5));

    }

    private static String generateUniqueEventType() {
        return "fahrschein.test-event." + System.currentTimeMillis();
    }

}