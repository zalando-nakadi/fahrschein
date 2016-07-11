package org.zalando.fahrschein.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.domain.Cursor;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collection;

import static java.lang.String.format;

public class JdbcCursorManager implements CursorManager {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcCursorManager.class);

    private final JdbcTemplate template;
    private final String consumerName;
    private final String schemaPrefix;

    public JdbcCursorManager(final JdbcTemplate template, final String consumerName, final String schema) {
        this.template = template;
        this.consumerName = consumerName;
        if (schema == null || schema.isEmpty()) {
            throw new IllegalArgumentException("schema may not be null or empty.");
        }
        this.schemaPrefix = schema + ".";
    }

    public JdbcCursorManager(final JdbcTemplate template, final String consumerName) {
        this.template = template;
        this.consumerName = consumerName;
        this.schemaPrefix = "";
    }


    public JdbcCursorManager(final DataSource dataSource, final String consumerName) {
        this(new JdbcTemplate(dataSource), consumerName);
    }

    @Override
    public void onSuccess(final String eventName, final Cursor cursor) throws IOException {
        final String sql = format("SELECT * FROM %snakadi_cursor_update(?, ?, ?, ?)", schemaPrefix);

        template.queryForObject(sql, new Object[]{consumerName, eventName, cursor.getPartition(), cursor.getOffset()}, Integer.class);
    }

    @Override
    public void onError(final String eventName, final Cursor cursor, final Throwable throwable) {
        LOG.warn("Exception while processing events for [{}] on partition [{}] at offset [{}]", eventName, cursor.getPartition(), cursor.getOffset(), throwable);
    }

    @Override
    public Collection<Cursor> getCursors(final String eventName) throws IOException {
        final String sql = format("SELECT * FROM %snakadi_cursor_find_by_event_name(?, ?)", schemaPrefix);

        return template.query(sql, new Object[]{consumerName, eventName}, (resultSet, i) -> {
            return new Cursor(resultSet.getString(2), resultSet.getString(3));
        });
    }
}
