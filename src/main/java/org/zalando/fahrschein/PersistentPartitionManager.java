package org.zalando.fahrschein;

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class PersistentPartitionManager implements PartitionManager {
    private static final Logger LOG = LoggerFactory.getLogger(PersistentCursorManager.class);

    private final JdbcTemplate template;

    public PersistentPartitionManager(DataSource dataSource) throws IOException {
        this(new JdbcTemplate(dataSource));
    }

    public PersistentPartitionManager(JdbcTemplate template) throws IOException {
        this.template = template;
    }

    private static String readResource(String resourceName) throws IOException {
        return Resources.toString(Resources.getResource(PersistentCursorManager.class, resourceName), StandardCharsets.UTF_8);
    }

    @Override
    public boolean lockPartition(String consumerName, String eventName, String partition, String lockedBy, long timeout, TimeUnit timeoutUnit) {
        final String newLockedBy = template.queryForObject("SELECT * FROM nakadi_cursor_partition_lock(?, ?, ?, ?, ?)", new Object[]{consumerName, eventName, partition, lockedBy, timeoutUnit.toMillis(timeout)}, String.class);
        return newLockedBy.equals(lockedBy);
    }

    @Override
    public void unlockPartition(String consumerName, String eventName, String partition, String lockedBy) {
        final int rows = template.queryForObject("SELECT * FROM nakadi_cursor_partition_unlock(?, ?, ?, ?)", new Object[]{consumerName, eventName, partition, lockedBy}, Integer.class);
        if (rows != 1) {
            throw new IllegalStateException("Unlock statement updated [" + rows + "] rows");
        }
    }

}
