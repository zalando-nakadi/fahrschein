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
