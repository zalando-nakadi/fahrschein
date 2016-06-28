WITH new_values AS (SELECT ? AS locked_by, ? * '1 millisecond'::interval AS lock_timeout, ? AS consumer_name, ? AS event_name, ? AS partition),
     upsert AS (
              UPDATE nakadi_cursor nc
                 SET nc_locked_by = locked_by,
                     nc_locked_until = now() + lock_timeout
               WHERE nc_consumer_name = consumer_name
                 AND nc_event_name = event_name
                 AND nc_partition = partition
                 AND (nc_locked_by IS NULL OR nc_locked_until < now())