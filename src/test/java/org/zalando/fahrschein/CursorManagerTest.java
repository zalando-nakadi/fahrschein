package org.zalando.fahrschein;

import org.junit.Before;
import org.junit.Test;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Partition;

import javax.annotation.Nullable;
import java.io.IOException;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CursorManagerTest {

    /*
    private static Object[] parameter(String name, @Nullable String initialOffset, String oldestAvailableOffset, String newestAvailableOffset, @Nullable String expectedOffset) {

    }
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {

        return asList(parameter(""))

    }
    */

    private final CursorManager cursorManager = mock(CursorManager.class);

    @Before
    public void setupMock() throws IOException {
        doCallRealMethod().when(cursorManager).updatePartitions(any(), any());
        doCallRealMethod().when(cursorManager).fromNewestAvailableOffsets(any(), any());
        doCallRealMethod().when(cursorManager).fromOldestAvailableOffset(any(), any());
    }

    private void run(@Nullable String initialOffset, String oldestAvailableOffset, String newestAvailableOffset, @Nullable String expectedOffset) throws IOException {
        when(cursorManager.getCursors("test")).thenReturn(initialOffset == null ? emptyList() : singletonList(new Cursor("0", initialOffset)));
        cursorManager.updatePartitions("test", singletonList(new Partition("0", oldestAvailableOffset, newestAvailableOffset)));
        if (expectedOffset != null) {
            verify(cursorManager).onSuccess("test", new Cursor("0", expectedOffset));
        } else {
            verify(cursorManager, never()).onSuccess(any(), any());
        }
    }

    @Test
    public void shouldNotUpdatePartitionWhenOffsetStillAvailable() throws IOException {
        run("20", "10", "30", null);
    }

    @Test
    public void shouldUpdatePartitionWhenNoCursorAndLastConsumedOffsetNoLongerAvailable() throws IOException {
        run(null, "10", "20", "BEGIN");
    }

    @Test
    public void shouldUpdatePartitionWhenLastConsumedOffsetNoLongerAvailable() throws IOException {
        run("5", "10", "20", "BEGIN");
    }

    @Test
    public void shouldUpdatePartitionToBeginWhenNoCursorAndPartitionIsEmpty() throws IOException {
        run(null, "0", "BEGIN", "BEGIN");
    }

    @Test
    public void shouldUpdatePartitionToNewestAvailableWhenNoCursorAndPartitionIsExpired() throws IOException {
        run(null, "2", "1", "BEGIN");
    }

    @Test
    public void shouldUpdatePartitionToNewestAvailableWhenPartitionIsExpired() throws IOException {
        run("10", "22", "21", "BEGIN");
    }

}
