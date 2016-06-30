CREATE OR REPLACE FUNCTION nakadi_cursor_partition_unlock(p_consumer_name text, p_event_name text, p_partition text, p_locked_by text) RETURNS INT AS
$$
BEGIN
    UPDATE nakadi_cursor nc
       SET nc_locked_by = NULL,
           nc_locked_until = NULL
     WHERE nc_locked_by = p_locked_by
       AND nc_consumer_name = p_consumer_name
       AND nc_event_name = p_event_name
       AND nc_partition = p_partition;

    RETURN FOUND::int;
END
$$ LANGUAGE 'plpgsql' VOLATILE SECURITY DEFINER;