package org.zalando.fahrschein;

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.zalando.fahrschein.domain.Cursor;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class PersistentCursorManager implements CursorManager {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentCursorManager.class);

    private final JdbcTemplate template;
    private final String update;
    private final String select;

    public PersistentCursorManager(JdbcTemplate template) {
        this.template = template;
        this.update = readResource("update_cursor.sql");
        this.select = readResource("select_cursors_by_event_name.sql");
    }

    private static String readResource(String resourceName) {
        try {
            return Resources.toString(Resources.getResource(PersistentCursorManager.class, resourceName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public PersistentCursorManager(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    @Override
    public void onSuccess(String eventName, Cursor cursor) {
        template.update(update, eventName, cursor.getPartition(), cursor.getOffset());
    }

    @Override
    public void onError(String eventName, Cursor cursor, EventProcessingException throwable) {
        LOG.warn("Exception while processing events for [{}] on partition [{}] at offset [{}]", eventName, cursor.getPartition(), cursor.getOffset(), throwable);
    }

    @Override
    public Collection<Cursor> getCursors(String eventName) {
        return template.query(select, new Object[]{eventName}, (resultSet, i) -> {
            return new Cursor(resultSet.getString(2), resultSet.getString(3));
        });
    }
}
