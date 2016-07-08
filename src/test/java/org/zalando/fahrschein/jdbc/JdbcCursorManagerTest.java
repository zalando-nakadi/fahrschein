package org.zalando.fahrschein.jdbc;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.zalando.fahrschein.domain.Cursor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JdbcCursorManagerTest {

    public static final String CONSUMER = "any-conusmer";
    public static final String EVENT_NAME = "any-event";
    public static final String SCHEMA = "zc_foobar";
    public static final Cursor CURSOR = new Cursor("don`t-care", "don`t-care");

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() throws Exception {
        jdbcTemplate = mock(JdbcTemplate.class);
    }

    @Test
    public void shouldExecuteSQLStatementForSuccessCallbackWithoutSchemaPrefix() throws Exception {

        new JdbcCursorManager(jdbcTemplate, CONSUMER).onSuccess(EVENT_NAME, CURSOR);

        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM nakadi_cursor_update(?, ?, ?, ?)"), any(), eq(Integer.class));
    }

    @Test
    public void shouldExecuteSQLStatementForFetchingCursorWithoutSchemaPrefix() throws Exception {

        new JdbcCursorManager(jdbcTemplate, CONSUMER).getCursors(EVENT_NAME);

        verify(jdbcTemplate).query(eq("SELECT * FROM nakadi_cursor_find_by_event_name(?, ?)"), any(Object[].class), any(RowMapper.class));
    }

    @Test
    public void shouldExecuteSQLStatementForSuccessCallbackWithPrefixedSchema() throws Exception {

        new JdbcCursorManager(jdbcTemplate, CONSUMER, SCHEMA).onSuccess(EVENT_NAME, CURSOR);

        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM " + SCHEMA + ".nakadi_cursor_update(?, ?, ?, ?)"), any(), eq(Integer.class));
    }

    @Test
    public void shouldExecuteSQLStatementForFetchingCursorWithPrefixedSchema() throws Exception {

        new JdbcCursorManager(jdbcTemplate, CONSUMER, SCHEMA).getCursors(EVENT_NAME);

        verify(jdbcTemplate).query(eq("SELECT * FROM " + SCHEMA + ".nakadi_cursor_find_by_event_name(?, ?)"), any(Object[].class), any(RowMapper.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldValidateSchema() throws Exception {
        new JdbcCursorManager(jdbcTemplate, CONSUMER, "");
    }
}
