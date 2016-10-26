CREATE OR REPLACE FUNCTION nakadi_partition_unlock(p_consumer_name text, p_event_name text, p_partitions text[], p_locked_by text) RETURNS TABLE(consumer_name text, event_name text, partition text) AS
$$

    UPDATE nakadi_partition np
       SET np_locked_by = NULL
      FROM unnest(p_partitions) partition
     WHERE np_locked_by = p_locked_by
       AND np_consumer_name = p_consumer_name
       AND np_event_name = p_event_name
       AND np_partition = partition
 RETURNING np_consumer_name, np_event_name, np_partition;

$$ LANGUAGE 'sql' VOLATILE SECURITY DEFINER;