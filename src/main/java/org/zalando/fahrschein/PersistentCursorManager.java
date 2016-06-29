package org.zalando.fahrschein;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.zalando.fahrschein.domain.Cursor;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collection;

public class PersistentCursorManager implements CursorManager {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentCursorManager.class);

    private final JdbcTemplate template;
    private final String consumerName;

    public PersistentCursorManager(JdbcTemplate template, String consumerName) {
        this.template = template;
        this.consumerName = consumerName;
    }

    public PersistentCursorManager(DataSource dataSource, String consumerName) {
        this(new JdbcTemplate(dataSource), consumerName);
    }

    @Override
    public void onSuccess(String eventName, Cursor cursor) throws IOException {
        template.queryForObject("SELECT * FROM nakadi_cursor_update(?, ?, ?, ?)", new Object[]{consumerName, eventName, cursor.getPartition(), cursor.getOffset()}, Integer.class);
    }

    @Override
    public void onError(String eventName, Cursor cursor, Throwable throwable) {
        LOG.warn("Exception while processing events for [{}] on partition [{}] at offset [{}]", eventName, cursor.getPartition(), cursor.getOffset(), throwable);
    }

    @Override
    public Collection<Cursor> getCursors(String eventName) throws IOException {
        return template.query("SELECT * FROM nakadi_cursor_find_by_event_name(?, ?)", new Object[]{consumerName, eventName}, (resultSet, i) -> {
            return new Cursor(resultSet.getString(2), resultSet.getString(3));
        });
    }
}
