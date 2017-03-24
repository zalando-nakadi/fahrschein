package org.zalando.fahrschein.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Partition;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.zalando.fahrschein.Preconditions.checkState;

public class JdbcCursorManager implements CursorManager {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcCursorManager.class);

    private final JdbcTemplate template;
    private final String consumerName;
    private final String schemaPrefix;

    public JdbcCursorManager(final JdbcTemplate template, final String consumerName, final String schema) {
        checkState(schema != null && !schema.isEmpty(), "Schema name should not be null or empty");
        this.template = template;
        this.consumerName = consumerName;
        this.schemaPrefix = schema + ".";
    }

    public JdbcCursorManager(final JdbcTemplate template, final String consumerName) {
        this.template = template;
        this.consumerName = consumerName;
        this.schemaPrefix = "";
    }

    public JdbcCursorManager(final DataSource dataSource, final String consumerName, final String schema) {
        this(new JdbcTemplate(dataSource), consumerName, schema);
    }

    public JdbcCursorManager(final DataSource dataSource, final String consumerName) {
        this(new JdbcTemplate(dataSource), consumerName);
    }

    @Override
    @Transactional
    public void onSuccess(final String eventName, final Cursor cursor) throws IOException {
        final String sql = format("SELECT * FROM %snakadi_cursor_update(?, ?, ?, ?)", schemaPrefix);

        template.queryForObject(sql, new Object[]{consumerName, eventName, cursor.getPartition(), cursor.getOffset()}, Integer.class);
    }

    @Override
    public Collection<Cursor> getCursors(final String eventName) throws IOException {
        final String sql = format("SELECT * FROM %snakadi_cursor_find_by_event_name(?, ?)", schemaPrefix);

        return template.query(sql, new Object[]{consumerName, eventName}, (resultSet, i) -> {
            final String partition = resultSet.getString(2);
            final String offset = resultSet.getString(3);
            return new Cursor(partition, offset);
        });
    }

    @Override
    @Transactional
    public void fromNewestAvailableOffsets(String eventName, List<Partition> partitions) throws IOException {
        CursorManager.super.fromNewestAvailableOffsets(eventName, partitions);
    }

    @Override
    @Transactional
    public void fromOldestAvailableOffset(String eventName, List<Partition> partitions) throws IOException {
        CursorManager.super.fromOldestAvailableOffset(eventName, partitions);
    }

    @Override
    @Transactional
    public void updatePartitions(String eventName, List<Partition> partitions) throws IOException {
        CursorManager.super.updatePartitions(eventName, partitions);
    }
}
