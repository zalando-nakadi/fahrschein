CREATE OR REPLACE FUNCTION nakadi_cursor_find_by_event_name(p_consumer_name text, p_event_name text) RETURNS TABLE(event_name text, partition text, "offset" text) AS
$$
    SELECT nc_event_name AS event_name, nc_partition AS partition, nc_offset AS "offset"
      FROM nakadi_cursor
     WHERE nc_consumer_name = p_consumer_name
       AND nc_event_name = p_event_name
$$ LANGUAGE 'sql' VOLATILE SECURITY DEFINER;