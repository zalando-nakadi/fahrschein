WITH new_values AS (
  SELECT ? AS event_name, ? AS partition, ? AS new_offset
),
upsert AS (
    UPDATE nakadi_cursor nc
        SET nc_offset = new_offset
    FROM new_values nv
    WHERE nc_event_name = event_name
      AND nc_partition = partition
    RETURNING nc.*
)
INSERT INTO nakadi_cursor (nc_event_name, nc_partition, nc_offset)
SELECT event_name, partition, new_offset
  FROM new_values
 WHERE NOT EXISTS (SELECT 1
                     FROM upsert up
                    WHERE nc_event_name = event_name
                      AND nc_partition = partition)