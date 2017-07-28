package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Partition;
import org.zalando.fahrschein.http.api.RequestFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LowLevelStreamBuilderTest {

    private final CursorManager cursorManager = mock(CursorManager.class);
    private final StreamBuilder.LowLevelStreamBuilder lowLevelStreamBuilder = new StreamBuilders.LowLevelStreamBuilderImpl(URI.create("http://example.com"), mock(RequestFactory.class), cursorManager, new ObjectMapper(), "test");

    private void run(@Nullable String initialOffset, String oldestAvailableOffset, String newestAvailableOffset, @Nullable String expectedOffset) throws IOException {
        when(cursorManager.getCursors("test")).thenReturn(initialOffset == null ? emptyList() : singletonList(new Cursor("0", initialOffset)));
        lowLevelStreamBuilder.skipUnavailableOffsets(singletonList(new Partition("0", oldestAvailableOffset, newestAvailableOffset)));
        if (expectedOffset != null) {
            verify(cursorManager).onSuccess(eq("test"), expectedCursors(expectedOffset));
        } else {
            verify(cursorManager, never()).onSuccess(any(), any(Cursor.class));
            verify(cursorManager, never()).onSuccess(any(), anyList());
        }
    }

    private static List<Cursor> expectedCursors(final String expectedOffset) {
        return argThat(new ArgumentMatcher<List<Cursor>>() {
            @Override
            public boolean matches(List<Cursor> argument) {
                return argument.size() == 1
                        && "0".equals(argument.get(0).getPartition())
                        && expectedOffset.equals(argument.get(0).getOffset());
            }
        });
    }

    @Test
    public void shouldNotUpdatePartitionWhenOffsetStillAvailable() throws IOException {
        run("20", "10", "30", null);
    }

    @Test
    public void shouldNotUpdatePartitionWhenOffsetStillAvailableAndMore() throws IOException {
        run("234", "12", "2345", null);
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
    public void shouldNotUpdatePartitionWhenCursorIsAreadyAtBegin() throws IOException {
        run("BEGIN", "0", "BEGIN", null);
    }

    @Test
    public void shouldUpdatePartitionToNewestAvailableWhenNoCursorAndPartitionIsExpired() throws IOException {
        run(null, "2", "1", "BEGIN");
    }

    @Test
    public void shouldUpdatePartitionToNewestAvailableWhenPartitionIsExpired() throws IOException {
        run("10", "22", "21", "BEGIN");
    }

    @Test
    public void shouldUpdatePartitionToNewestAvailableWhenPartitionIsExpiredLongAgo() throws IOException {
        run("10", "1234", "2345", "BEGIN");
    }

}
