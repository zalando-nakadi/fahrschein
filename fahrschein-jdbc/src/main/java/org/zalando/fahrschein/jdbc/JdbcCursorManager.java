package org.zalando.fahrschein.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.domain.Cursor;
import org.zalando.fahrschein.domain.Subscription;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.zalando.fahrschein.Preconditions.checkState;

public class JdbcCursorManager implements CursorManager {

    private static final String FIND_BY_EVENT_NAME = "SELECT * FROM %snakadi_cursor_find_by_event_name(?, ?)";
    private static final String UPDATE = "SELECT * FROM %snakadi_cursor_update(?, ?, ?, ?)";

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
    public void addSubscription(Subscription subscription) {

    }

    @Override
    public void addStreamId(Subscription subscription, String streamId) {

    }

    @Override
    @Transactional
    public void onSuccess(final String eventName, final Cursor cursor) throws IOException {
        final String sql = format(UPDATE, schemaPrefix);
        final Object[] params = mapParams(eventName, cursor);

        template.queryForObject(sql, params, Integer.class);
    }

    @Override
    @Transactional
    public void onSuccess(final String eventName, final List<Cursor> cursors) throws IOException {
        final String sql = format(UPDATE, schemaPrefix);
        final List<Object[]> params = new ArrayList<>(cursors.size());
        for (Cursor cursor : cursors) {
            params.add(mapParams(eventName, cursor));
        }

        template.batchUpdate(sql, params);
    }

    private Object[] mapParams(String eventName, Cursor cursor) {
        return new Object[]{consumerName, eventName, cursor.getPartition(), cursor.getOffset()};
    }

    @Override
    public Collection<Cursor> getCursors(final String eventName) throws IOException {
        final String sql = format(FIND_BY_EVENT_NAME, schemaPrefix);

        return template.query(sql, new Object[]{consumerName, eventName}, new RowMapper<Cursor>() {
            @Override
            public Cursor mapRow(ResultSet resultSet, int i) throws SQLException {
                final String partition = resultSet.getString(2);
                final String offset = resultSet.getString(3);
                return new Cursor(partition, offset);
            }
        });
    }

}
