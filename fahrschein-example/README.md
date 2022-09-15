## Fahrschein Examples

These examples need a locally running Nakadi with the necessary event-types set up. First, start Nakadi via docker-compose:

```sh
docker-compose up -d fahrschein-example/src/test/resources/docker-compose.yaml
```

Then, create the event types:

```sh
fahrschein-example/scripts/create-event-types.sh
```


### Producer Example

The `ProducerExample` code showcases the usage of publishing events with OpenTelemetry integration.

### Consumer Example

The `ConsumerExample` code can be run in parallel to the `ProducerExample`, or accompanied by the
`produce-example-events.sh` shell script. It showcases the consumption of multiple event types with
one consumer, and the integration into OpenTelemetry using the `OpenTelemetryWrapper`.