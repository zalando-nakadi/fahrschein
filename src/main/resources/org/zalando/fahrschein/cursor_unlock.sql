UPDATE nakadi_cursor nc
   SET nc_locked_by = NULL,
       nc_locked_until = NULL
 WHERE nc_locked_by = ?
   AND nc_consumer_name = ?
   AND nc_event_name = ?
   AND nc_partition = ?