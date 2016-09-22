CREATE OR REPLACE FUNCTION nakadi_cursor_update(p_consumer_name text, p_event_name text, p_partition text, p_offset text) RETURNS VOID AS
$$
    WITH updated AS (UPDATE nakadi_cursor nc
                        SET nc_offset = p_offset,
                            nc_last_modified = now()
                      WHERE nc_consumer_name = p_consumer_name
                        AND nc_event_name = p_event_name
                        AND nc_partition = p_partition
                  RETURNING nc.*)
    INSERT INTO nakadi_cursor (nc_consumer_name, nc_event_name, nc_partition, nc_offset)
    SELECT p_consumer_name, p_event_name, p_partition, p_offset
     WHERE NOT EXISTS (SELECT 1
                         FROM updated up
                        WHERE nc_event_name = p_event_name
                          AND nc_consumer_name = p_consumer_name
                          AND nc_partition = p_partition);
$$ LANGUAGE 'sql' VOLATILE SECURITY DEFINER;
