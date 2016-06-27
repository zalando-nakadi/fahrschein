SELECT nc_event_name AS event_name, nc_partition AS partition, nc_offset
  FROM nakadi_cursor
 WHERE nc_consumer_name = ?
   AND nc_event_name = ?
