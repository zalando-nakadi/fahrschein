CREATE OR REPLACE FUNCTION nakadi_cursor_partition_lock(p_consumer_name text, p_event_name text, p_partition text, p_locked_by text, p_lock_timeout bigint) RETURNS text AS
$$
DECLARE
    l_prev_locked_by    text;
    l_prev_locked_until timestamp without time zone;
    l_locked_until      timestamp without time zone := now() + (p_lock_timeout * '1 millisecond'::interval);
    l_locked_by         text;
BEGIN

     SELECT nc_locked_by, nc_locked_until
       INTO l_prev_locked_by, l_prev_locked_until
       FROM nakadi_cursor
      WHERE nc_consumer_name = p_consumer_name
        AND nc_event_name = p_event_name
        AND nc_partition = p_partition
        FOR UPDATE;

     IF l_prev_locked_by IS NULL OR l_prev_locked_until < statement_timestamp() THEN

               WITH updated AS (UPDATE nakadi_cursor nc
                                   SET nc_locked_by = p_locked_by,
                                       nc_locked_until = l_locked_until
                                 WHERE nc_consumer_name = p_consumer_name
                                   AND nc_event_name = p_event_name
                                   AND nc_partition = p_partition
                                RETURNING nc_consumer_name, nc_event_name, nc_partition, nc_locked_by, nc_locked_until),
                    inserted AS (INSERT INTO nakadi_cursor (nc_consumer_name, nc_event_name, nc_partition, nc_locked_by, nc_locked_until)
                                 SELECT p_consumer_name, p_event_name, p_partition, p_locked_by, l_locked_until
                                  WHERE NOT EXISTS (SELECT 1 FROM updated)
                                  RETURNING nc_consumer_name, nc_event_name, nc_partition, nc_locked_by, nc_locked_until)
               SELECT COALESCE((SELECT nc_locked_by FROM updated), (SELECT nc_locked_by FROM inserted))
                 INTO l_locked_by;

            RETURN l_locked_by;
     ELSE
            RETURN l_prev_locked_by;
     END IF;
END
$$ LANGUAGE 'plpgsql' VOLATILE SECURITY DEFINER;