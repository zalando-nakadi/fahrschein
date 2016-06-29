CREATE TABLE nakadi_cursor (

    nc_consumer_name  text NOT NULL,
    nc_event_name     text NOT NULL,
    nc_partition      text NOT NULL,
    nc_offset         text NOT NULL DEFAULT 'BEGIN',
    nc_locked_by      text,
    nc_locked_until   timestamp without time zone,
    nc_last_modified  timestamp without time zone NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (nc_consumer_name, nc_event_name, nc_partition)
);

CREATE OR REPLACE FUNCTION nakadi_cursor_find_by_event_name(p_consumer_name text, p_event_name text) RETURNS TABLE(event_name text, partition text, "offset" text) AS
$$
    SELECT nc_event_name AS event_name, nc_partition AS partition, nc_offset AS "offset"
      FROM nakadi_cursor
     WHERE nc_consumer_name = p_consumer_name
       AND nc_event_name = p_event_name
$$ LANGUAGE 'sql' VOLATILE;


CREATE OR REPLACE FUNCTION nakadi_cursor_update(p_consumer_name text, p_event_name text, p_partition text, p_offset text) RETURNS VOID AS
$$
    WITH updated AS (UPDATE nakadi_cursor nc
                        SET nc_offset = p_offset
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
$$ LANGUAGE 'sql' VOLATILE;


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
$$ LANGUAGE 'plpgsql' VOLATILE;


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
$$ LANGUAGE 'plpgsql' VOLATILE;