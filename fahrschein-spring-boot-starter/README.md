# Fahrschein Spring-Boot-Starter

*Fahrschein Spring Boot Starter* is a library that integrates the Fahrschein Nakadi 
client library into a Spring Boot environment. It sets up Nakadi clients and make them 
configurable without writing boilerplate code.

- **Technology stack**: Spring Boot (3.x), JDK 17
- **Status**:  Beta

## Example

```yaml
fahrschein:
  defaults:
    nakadi-url: http://localhost:8080
    application-name: example-application
    consumer-group: example-consumergroup
    stream-parameters:
      batch-limit: 1000
      max-uncommitted-events: 5000
      batch-flush-timeout: 5
    authorizations:
      anyReader: false
      admins:
        services:
          - some_service_identifier
        users:
          - user_6
          - user_2
        teams:
          - team_id_1
          - team_id_2
      readers:
        services:
          - some_other_service_identifier
    oauth:
      enabled: true
      credentials-directory: /meta/credentials
      access-token-id: nakadi
    record-metrics: true
    http:
      content-encoding: gzip
    backoff:
      enabled: true
  consumers:
    article-01:
      topics:
      - 'article-example_01924c48-AAAA-40c2-DDDD-ab582e6db6f4_ms'
      stream-parameters:
        batch-limit: 5
        max-uncommitted-events: 20
      backoff:
        enabled: false
    article-02:
      topics:
      - 'article-example_00f2a393-AAAA-4fc0-DDDD-86e454e6dfa3_ag'
    article-03:
      topics:
      - 'article-example_091dcbdd-AAAA-4f39-DDDD-324eb4599df0_io'
      - 'article-example_7ce94f55-AAAA-4416-DDDD-bf34193a47e8_co'
      - 'article-example_ebf57ebf-AAAA-4ebd-DDDD-6ad519073d2a_us'
      stream-parameters:
        batch-limit: 30
        max-uncommitted-events: 500
```

## Installation

Add the following dependency to your project:

```
    <dependency>
        <groupId>org.zalando</groupId>
        <artifactId>fahrschein-spring-boot-starter</artifactId>
        <version>${version}</version>
    </dependency>
```


Clients are identified by a *Client ID*, for instance `example` in the sample above. You can have as many clients as you want.

### Reference

For a complete overview of available properties, the type and default value, please refer to the following table:

| Configuration                                 | Data type         | Default / Comment                                                                                                |
|-----------------------------------------------|-------------------|------------------------------------------------------------------------------------------------------------------|
| `fahrschein`                                  |                   |                                                                                                                  |
| `├── defaults`                                |                   |                                                                                                                  |
| `│   ├── nakadi-url`                          | `String`          | none                                                                                                             |
| `│   ├── application-name`                    | `String`          | none                                                                                                             |
| `│   ├── authorizations`                      |                   | none                                                                                                             |
| `│   │   ├── admins`                          |                   | none                                                                                                             |
| `│   │   │   ├── users`                       | `List<String>`    | none                                                                                                             |
| `│   │   │   ├── services`                    | `List<String>`    | none                                                                                                             |
| `│   │   │   └── teams`                       | `List<String>`    | none                                                                                                             |
| `│   │   ├── readers`                         |                   | none                                                                                                             |
| `│   │   │   ├── users`                       | `List<String>`    | none                                                                                                             |
| `│   │   │   ├── services`                    | `List<String>`    | none                                                                                                             |
| `│   │   │   └── teams`                       | `List<String>`    | none                                                                                                             |
| `│   │   └── writers`                         |                   | none                                                                                                             |
| `│   │       ├── users`                       | `List<String>`    | none                                                                                                             |
| `│   │       ├── services`                    | `List<String>`    | none                                                                                                             |
| `│   │       └── teams`                       | `List<String>`    | none                                                                                                             |
| `│   ├── consumer-group`                      | `String`          | none                                                                                                             |
| `│   ├── autostart-enabled`                   | `boolean`         | `true`                                                                                                           |
| `│   ├── record-metrics`                      | `boolean`         | `false`                                                                                                          |
| `│   ├── read-from`                           | `Position`        | `end` (`begin`)                                                                                                  |
| `│   ├── oauth`                               |                   |                                                                                                                  |
| `│   │   ├── enabled`                         | `boolean`         | `false`                                                                                                          |
| `│   │   └── access-token-id`                 | `String`          | none                                                                                                             |
| `│   ├── http`                                |                   |                                                                                                                  |
| `│   │   ├── socket-timeout`                  | `TimeSpan`        | `5 seconds`                                                                                                      |
| `│   │   ├── connect-timeout`                 | `TimeSpan`        | `5 seconds`                                                                                                      |
| `│   │   ├── connection-request-timeout`      | `TimeSpan`        | `5 seconds`                                                                                                      |
| `│   │   ├── content-encoding`                | `ContentEncoding` | `gzip` (`identity`)                                                                                              |
| `│   │   ├── content-compression-enabled`     | `boolean`         | `false`                                                                                                          |
| `│   │   ├── buffer-size`                     | `int`             | `512`                                                                                                            |
| `│   │   ├── connection-time-to-live`         | `TimeSpan`        | `30 seconds`                                                                                                     |
| `│   │   ├── max-connections-total`           | `int`             | `3`                                                                                                              |
| `│   │   ├── max-connections-per-route`       | `int`             | `3`                                                                                                              |
| `│   │   ├── evict-expired-connections`       | `boolean`         | `true`                                                                                                           |
| `│   │   ├── evict-idle-connections`          | `boolean`         | `true`                                                                                                           |
| `│   │   └── max-idle-time`                   | `int`             | `10_000`                                                                                                         |
| `│   ├── backoff`                             |                   |                                                                                                                  |
| `│   │   ├── enabled`                         | `boolean`         | `false`                                                                                                          |
| `│   │   ├── intial-delay`                    | `TimeSpan`        | `500 milliseconds`                                                                                               |
| `│   │   ├── max-delay`                       | `TimeSpan`        | `10 minutes`                                                                                                     |
| `│   │   ├── backoff-factor`                  | `double`          | `1.5`                                                                                                            |
| `│   │   ├── max-retries`                     | `int`             | `1`                                                                                                              |
| `│   │   └── jitter`                          |                   |                                                                                                                  |
| `│   │       ├── enabled`                     | `boolean`         | `false`                                                                                                          |
| `│   │       └── type`                        | `JitterType`      | `equal` (`full`)                                                                                                 |
| `│   ├── threads`                             |                   |                                                                                                                  |
| `│   │   └── listener-pool-size`              | `int`             | `1`                                                                                                              |
| `│   ├── oauth`                               |                   |                                                                                                                  |
| `│   │   ├── enabled`                         | `boolean`         | `false`                                                                                                          |
| `│   │   └── access-token-id`                 | `String`          | none                                                                                                             |
| `│   └── stream-parameters`                   |                   |                                                                                                                  |
| `│       ├── batch-limit`                     | `int`             | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*batch_limit)             |                                                                    
| `│       ├── stream-limit`                    | `int`             | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*stream_limit)            |                                                                      
| `│       ├── batch-flush-timeout`             | `TimeSpan`        | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*batch_flush_timeout)     |
| `│       ├── stream-timeout`                  | `TimeSpan`        | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*stream_timeout)          |
| `│       ├── commit-timeout`                  | `TimeSpan`        | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*commit_timeout)          |
| `│       ├── stream-keep-alive-limit`         | `int`             | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*stream_keep_alive_limit) |
| `│       ├── batch-timespan`                  | `TimeSpan`        | none                                                                                                             |
| `│       └── max-uncommitted-events`          | `int`             | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*max_uncommitted_events)  |
| `│`                                           |                   |                                                                                                                  |
| `└── consumers`                               |                   |                                                                                                                  |
| `    └── <id>`                                | `String`          |                                                                                                                  |
| `        ├── topics`                          | `List<String>`    |                                                                                                                  |
| `        ├── nakadi-url`                      | `String`          | none                                                                                                             |
| `        ├── application-name`                | `String`          | none                                                                                                             |
| `        ├── authorizations`                  |                   | none                                                                                                             |
| `        │   ├── admins`                      |                   | none                                                                                                             |
| `        │   │   ├── users`                   | `List<String>`    | none                                                                                                             |
| `        │   │   ├── services`                | `List<String>`    | none                                                                                                             |
| `        │   │   └── teams`                   | `List<String>`    | none                                                                                                             |
| `        │   ├── readers`                     |                   | none                                                                                                             |
| `        │   │   ├── users`                   | `List<String>`    | none                                                                                                             |
| `        │   │   ├── services`                | `List<String>`    | none                                                                                                             |
| `        │   │   └── teams`                   | `List<String>`    | none                                                                                                             |
| `        │   └── writers`                     |                   | none                                                                                                             |
| `        │       ├── users`                   | `List<String>`    | none                                                                                                             |
| `        │       ├── services`                | `List<String>`    | none                                                                                                             |
| `        │       └── teams`                   | `List<String>`    | none                                                                                                             |
| `        ├── consumer-group`                  | `String`          | none                                                                                                             |
| `        ├── autostart-enabled`               | `boolean`         | `true`                                                                                                           |
| `        ├── record-metrics`                  | `boolean`         | `false`                                                                                                          |
| `        ├── read-from`                       | `Position`        | `end` (`begin`)                                                                                                  |
| `        ├── oauth`                           |                   |                                                                                                                  |
| `        │   ├── enabled`                     | `boolean`         | `false`                                                                                                          |
| `        │   └── access-token-id`             | `String`          | none                                                                                                             |
| `        ├── http`                            |                   |                                                                                                                  |
| `        │   ├── socket-timeout`              | `TimeSpan`        | `5 seconds`                                                                                                      |
| `        │   ├── connect-timeout`             | `TimeSpan`        | `5 seconds`                                                                                                      |
| `        │   ├── connection-request-timeout`  | `TimeSpan`        | `5 seconds`                                                                                                      |
| `        │   ├── content-encoding`            | `ContentEncoding` | `gzip` (`identity`)                                                                                              |
| `        │   ├── content-compression-enabled` | `boolean`         | `false`                                                                                                          |
| `        │   ├── buffer-size`                 | `int`             | `512`                                                                                                            |
| `        │   ├── connection-time-to-live`     | `TimeSpan`        | `30 seconds`                                                                                                     |
| `        │   ├── max-connections-total`       | `int`             | `3`                                                                                                              |
| `        │   ├── max-connections-per-route`   | `int`             | `3`                                                                                                              |
| `        │   ├── evict-expired-connections`   | `boolean`         | `true`                                                                                                           |
| `        │   ├── evict-idle-connections`      | `boolean`         | `true`                                                                                                           |
| `        │   └── max-idle-time`               | `int`             | `10_000`                                                                                                         |
| `        ├── backoff`                         |                   |                                                                                                                  |
| `        │   ├── enabled`                     | `boolean`         | `false`                                                                                                          |
| `        │   ├── intial-delay`                | `TimeSpan`        | `500 milliseconds`                                                                                               |
| `        │   ├── max-delay`                   | `TimeSpan`        | `10 minutes`                                                                                                     |
| `        │   ├── backoff-factor`              | `double`          | `1.5`                                                                                                            |
| `        │   ├── max-retries`                 | `int`             | `1`                                                                                                              |
| `        │   └── jitter`                      |                   |                                                                                                                  |
| `        │       ├── enabled`                 | `boolean`         | `false`                                                                                                          |
| `        │       └── type`                    | `JitterType`      | `equal` (`full`)                                                                                                 |
| `        ├── threads`                         |                   |                                                                                                                  |
| `        │   └── listener-pool-size`          | `int`             | `1`                                                                                                              |
| `        ├── oauth`                           |                   |                                                                                                                  |
| `        │   ├── enabled`                     | `boolean`         | `false`                                                                                                          |
| `        │   └── access-token-id`             | `String`          | none                                                                                                             |
| `        ├── stream-parameters`               |                   |                                                                                                                  |
| `        │   ├── batch-limit`                 | `int`             | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*batch_limit)             |
| `        │   ├── stream-limit`                | `int`             | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*stream_limit)            |
| `        │   ├── batch-flush-timeout`         | `TimeSpan`        | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*batch_flush_timeout)     |
| `        │   ├── stream-timeout`              | `TimeSpan`        | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*stream_timeout)          |
| `        │   ├── commit-timeout`              | `TimeSpan`        | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*commit_timeout)          |
| `        │   ├── stream-keep-alive-limit`     | `int`             | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*stream_keep_alive_limit) |
| `        │   ├── batch-timespan`              | `TimeSpan`        | none                                                                                                             |
| `        │   └── max-uncommitted-events`      | `int`             | [default value](https://nakadi.io/manual.html#/subscriptions/subscription_id/events_get*max_uncommitted_events)  |
| `        └── subscription-by-id`              | `String`          | none. (Existing subscription ID, takes precedence over registering a subscription by consumer group)             |
