package org.zalando.fahrschein.test;

import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.domain.Cursor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Transactional
public abstract class AbstractCursorManagerTest {


    protected abstract CursorManager cursorManager();

    @Test
    public void shouldBeEmptyByDefault() throws IOException {
        final Collection<Cursor> cursors = cursorManager().getCursors("test");
        assertNotNull(cursors);
        assertTrue(cursors.isEmpty());
    }


    @Test
    public void shouldCreateCursorOnSuccess() throws IOException {
        cursorManager().onSuccess("test", new Cursor("0", "123"));

        final Collection<Cursor> cursors = cursorManager().getCursors("test");
        assertNotNull(cursors);
        assertEquals(1, cursors.size());

        final Cursor cursor = cursors.iterator().next();
        assertEquals("0", cursor.getPartition());
        assertEquals("123", cursor.getOffset());
    }

    @Test
    public void shouldUpdateCursorOnSuccess() throws IOException {
        cursorManager().onSuccess("test", new Cursor("0", "123"));

        {
            final Collection<Cursor> cursors = cursorManager().getCursors("test");
            assertNotNull(cursors);
            assertEquals(1, cursors.size());

            final Cursor cursor = cursors.iterator().next();
            assertEquals("0", cursor.getPartition());
            assertEquals("123", cursor.getOffset());
        }

        cursorManager().onSuccess("test", new Cursor("0", "124"));
        {
            final Collection<Cursor> cursors = cursorManager().getCursors("test");
            assertNotNull(cursors);
            assertEquals(1, cursors.size());

            final Cursor cursor = cursors.iterator().next();
            assertEquals("0", cursor.getPartition());
            assertEquals("124", cursor.getOffset());
        }
    }

    @Test
    public void shouldCreateCursorsForMultiplePartitions() throws IOException {
        cursorManager().onSuccess("test", new Cursor("0", "12"));
        cursorManager().onSuccess("test", new Cursor("1", "13"));

        final List<Cursor> cursors = new ArrayList<>(cursorManager().getCursors("test"));
        Collections.sort(cursors, new Comparator<Cursor>() {
            @Override
            public int compare(Cursor o1, Cursor o2) {
                return o1.getPartition().compareTo(o2.getPartition());
            }
        });

        assertNotNull(cursors);
        assertEquals(2, cursors.size());

        {
            final Cursor cursor = cursors.get(0);
            assertEquals("0", cursor.getPartition());
            assertEquals("12", cursor.getOffset());
        }
        {
            final Cursor cursor = cursors.get(1);
            assertEquals("1", cursor.getPartition());
            assertEquals("13", cursor.getOffset());
        }
    }

}
