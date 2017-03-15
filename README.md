# Fahrschein

> You need a fahrschein to use the [nakadi](https://github.com/zalando/nakadi) event bus.

[![Fahrschein](docs/Bundesarchiv_Bild_183-1990-0104-025,_BVG-Fahrscheine.jpg)](https://commons.wikimedia.org/wiki/File:Bundesarchiv_Bild_183-1990-0104-025,_BVG-Fahrscheine.jpg)

*Image Source: Deutsches Bundesarchiv, Photographer: Ralph Hirschberger, Image License: [CC BY-SA 3.0 DE](https://creativecommons.org/licenses/by-sa/3.0/de/deed.en)*

[![Build Status](https://travis-ci.org/zalando-incubator/fahrschein.svg?branch=master)](https://travis-ci.org/zalando-incubator/fahrschein)
[![Release](https://img.shields.io/github/release/zalando-incubator/fahrschein.svg)](https://github.com/zalando-incubator/fahrschein/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/fahrschein.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/fahrschein)

## Features

 - Consistent error handling
    - `IOException` handled as retryable
    - `EventAlreadyProcessedException` is ignored
    - `RuntimeException` aborts processing and can be handled outside the main loop
 - Stream-based parsing
    - Optimized utf-8 decoding by [Jackson](https://github.com/FasterXML/jackson)
    - No unnecessary buffering or line-based processing, causing less garbage
    - Less garbage and higher performance
    - No required base classes for events
 - Support for both high-level (subscription) and low-level apis
 - Pluggable HTTP client implementations using [`ClientHttpRequestFactory`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/http/client/ClientHttpRequestFactory.html) interface

## Installation

Fahrschein is available in maven central, so you only have to add the following dependency to your project:

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>fahrschein</artifactId>
    <version>${fahrschein.version}</version>
</dependency>
```

## Usage

```java
final String eventName = "sales-order-placed";

// Create a Listener for our event
final Listener<SalesOrderPlaced> listener = events -> {
    for (SalesOrderPlaced salesOrderPlaced : events) {
        LOG.info("Received sales order [{}]", salesOrderPlaced.getSalesOrder().getOrderNumber());
    }
};

// Configure client, defaults to using the high level api with ManagedCursorManger and SimpleClientHttpRequestFactory
final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
        .withAccessTokenProvider(new ZignAccessTokenProvider())
        .build();

// Create subscription using the high level api
Subscription subscriptions = nakadiClient.subscription(applicationName, eventName).subscribe();

// Start streaming, the listen call will block and automatically reconnect on IOException
nakadiClient.stream(subscription)
        .listen(SalesOrderPlaced.class, listener);

```

See [`Main.java`](src/test/java/org/zalando/fahrschein/salesorder/Main.java) for an executable version of the above code.

## Fahrschein compared to other nakadi client libraries

|                      | Fahrschein                                                        | Nakadi-Klients        | Reactive-Nakadi         | Straw               |
| -------------------- | ----------------------------------------------------------------- | --------------------- | ----------------------- | ------------------- |
| Dependencies         | Spring (http client and jdbc), Jackson                            | Scala, Akka, Jackson  | Scala, Akka             | None                |
| Cursor Management    | In-Memory / Persistent (Postgres or Redis)                        | In-Memory             | Persistent (Dynamo)     |                     |
| Partition Management | In-Memory / Persistent (Postgres)                                 |                       | Persistent (Dynamo) (?) |                     |
| Error Handling       | Automatic reconnect with exponential backoff                      | Automatic reconnect   | (?)                     | No error handling   |

## Initializing partition offsets

By default nakadi will start streaming from the most recent offset. The initial offsets can be changed by requesting data about partitions from Nakadi and using this data to configure `CursorManager`.

```java
final List<Partition> partitions = nakadiClient.getPartitions(eventName);

// The cursor manager can be configured to start reading from the oldest available offset in each partition
cursorManager.fromOldestAvailableOffset(eventName, partitions);

// Or from the newest available offset, but this is the same as the default
cursorManager.fromNewestAvailableOffsets(eventName, partitions);

// Or (for a persistent cursor manager) we can start reading from the last offset that we processed if it's still available, and from the oldest available offset otherwise
cursorManager.updatePartitions(eventName, partitions);
```

## Using the low-level api

You can also use the low-level api, which requires local persistence of partition offsets. There are persistent `CursorManager` implementations using either Postgres or Redis.

```java
final HikariConfig hikariConfig = new HikariConfig();
hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/local_nakadi_cursor_db");
hikariConfig.setUsername("postgres");
hikariConfig.setPassword("postgres");
final DataSource dataSource = new HikariDataSource(hikariConfig);

final CursorManager cursorManager = new JdbcCursorManager(dataSource, "fahrschein-demo");

final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
        .withAccessTokenProvider(new ZignAccessTokenProvider())
        .withCursorManager(cursorManager)
        .build();

nakadiClient.stream(eventName)
        .listen(SalesOrderPlaced.class, listener);
```

## Using multiple partitions and multiple consumers

With the `PartitionManager` api it is possible to coordinate between multiple nodes of one application, so that only one node is consuming events from a partition at the same time.

Partitions are locked by one node for a certain time. This requires that every node has an unique name or other identifier.

```java
@Scheduled(fixedDelay = 60*1000L)
public void readSalesOrderPlacedEvents() throws IOException {
    final String lockedBy = ... // host name or another unique identifier for this node
    final List<Partition> partitions = nakadiClient.getPartitions(eventName);
    final Optional<Lock> lock = partitionManager.lockPartitions(eventName, partitions, lockedBy);

    if (optionalLock.isPresent()) {
        final Lock lock = optionalLock.get();
        try {
            nakadiClient.stream(eventName)
                    .withLock(lock))
                    .listen(SalesOrderPlaced.class, listener);
        } finally {
            partitionManager.unlockPartitions(lock);
        }
    }
}
```

## Exception handling

Exception handling while streaming events follows some simple rules

 - `IOException` and its subclasses are treated as temporary failures and will be retried as specified by the `BackoffStrategy`
 - `RuntimeException` aborts streaming of events, the user is responsible for handling these
 - If an `IOException` happens when opening the initial connection, this is not retried as it probably indicates a configuration problem (wrong host name or missing scopes)
 - Exceptions in other client methods are not automatically retried

### Handling data binding problems

You might want to ignore events that could not be mapped to your domain objects by Jackson, instead of having these events block all further processing. To achieve this you can override the `onMappingException` method of `Listener` and handle the `JsonMappingException` yourself.

## Using another ClientHttpRequestFactory

This library is currently tested and used in production with `SimpleClientHttpRequestFactory` and `HttpComponentsClientHttpRequestFactory`.

Please note that `HttpComponentsClientHttpRequestFactory` and also `SimpleClientHttpRequestFactory` since spring 4.3.x try to consume the remaining stream on closing and so might block during reconnection.

## Getting help

If you have questions, concerns, bug reports, etc, please file an issue in this repository's issue tracker.

## Getting involved

To contribute, simply make a pull request and add a brief description (1-2 sentences) of your addition or change.
For more details check the [contribution guidelines](CONTRIBUTING.md).
