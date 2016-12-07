CREATE OR REPLACE FUNCTION nakadi_partition_update(p_consumer_name text, p_event_name text, p_partitions text[]) RETURNS TABLE(consumer_name text, event_name text, partition text) AS
$$
BEGIN;



    INSERT INTO nakadi_cursor (nc_consumer_name, nc_event_name, nc_partition)
    SELECT p_consumer_name, p_event_name, partition
      FROM unnest(p_partitions) AS partition
     WHERE NOT EXISTS (SELECT 1
                         FROM nakadi_cursor
                        WHERE nc_consumer_name = p_consumer_name
                          AND nc_event_name = p_event_name
                          AND nc_partition = partition)
    RETURNING nc_consumer_name, nc_event_name, nc_partition;

$$ LANGUAGE 'sql' VOLATILE SECURITY DEFINER;