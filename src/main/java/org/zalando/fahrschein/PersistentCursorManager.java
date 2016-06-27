package org.zalando.fahrschein;

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.zalando.fahrschein.domain.Cursor;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class PersistentCursorManager implements CursorManager {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentCursorManager.class);

    private final JdbcTemplate template;
    private final String consumerName;
    private final String update;
    private final String select;

    public PersistentCursorManager(JdbcTemplate template, String consumerName) throws IOException {
        this.template = template;
        this.consumerName = consumerName;
        this.update = readResource("update_cursor.sql");
        this.select = readResource("select_cursors_by_event_name.sql");
    }

    private static String readResource(String resourceName) throws IOException {
        return Resources.toString(Resources.getResource(PersistentCursorManager.class, resourceName), StandardCharsets.UTF_8);
    }

    public PersistentCursorManager(DataSource dataSource, String consumerName) throws IOException {
        this(new JdbcTemplate(dataSource), consumerName);
    }

    @Override
    public void onSuccess(String eventName, Cursor cursor) throws IOException {
        template.update(update, consumerName, eventName, cursor.getPartition(), cursor.getOffset());
    }

    @Override
    public void onError(String eventName, Cursor cursor, Throwable throwable) {
        LOG.warn("Exception while processing events for [{}] on partition [{}] at offset [{}]", eventName, cursor.getPartition(), cursor.getOffset(), throwable);
    }

    @Override
    public Collection<Cursor> getCursors(String eventName) throws IOException {
        return template.query(select, new Object[]{consumerName, eventName}, (resultSet, i) -> {
            return new Cursor(resultSet.getString(2), resultSet.getString(3));
        });
    }
}
