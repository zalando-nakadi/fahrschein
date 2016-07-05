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
    - `RuntimeException` aborts processing and can be handled outside the main loop
 - No unnecessary buffering or line-based processing
    - Less garbage and higher performance
    - All processing done by [Jackson](https://github.com/FasterXML/jackson) json parser

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
final URI baseUri = new URI("https://nakadi-sandbox-hila.aruha-test.zalan.do");
final String eventName = "sales-order-service.order-placed";

// Create an ObjectMapper that understands Nakadi naming conventions and problem responses
final ObjectMapper objectMapper = new ObjectMapper();
objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
objectMapper.registerModule(new ProblemModule());

// Create a ClientHttpRequestFactory that automatically sends "Authorization: Bearer TOKEN" headers and handles proble responses
final SimpleClientHttpRequestFactory requestFactoryDelegate = new SimpleClientHttpRequestFactory();
final ProblemHandlingClientHttpRequestFactory problemHandlingRequestFactory = new ProblemHandlingClientHttpRequestFactory(requestFactoryDelegate, objectMapper);
final ClientHttpRequestFactory requestFactory = new AuthorizedClientHttpRequestFactory(problemHandlingRequestFactory, () -> "MY_ACCESS_TOKEN");

final CursorManager cursorManager = new InMemoryCursorManager();
final ExponentialBackoffStrategy exponentialBackoffStrategy = new ExponentialBackoffStrategy();
final StreamParameters streamParameters = new StreamParameters();
final NakadiClient nakadiClient = new NakadiClient(baseUri, requestFactory, exponentialBackoffStrategy, objectMapper, cursorManager);

// Create a listener for our event
final Listener<SalesOrderPlaced> listener = events -> {
    for (SalesOrderPlaced salesOrderPlaced : events) {
        LOG.info("Received sales order [{}]", salesOrderPlaced.getSalesOrder().getOrderNumber());
    }
};

// Start streaming, the listen call will block and automatically reconnect on IOException
nakadiClient.listen(eventName, SalesOrderPlaced.class, listener, streamParameters);
```

See [`Main.java`](src/test/java/org/zalando/fahrschein/salesorder/Main.java) for an executable version of the above code.

## Fahrschein compared to other nakadi client libraries

|                      | Fahrschein                                                        | Nakadi-Klients        | Reactive-Nakadi         | Straw               |
| -------------------- | ----------------------------------------------------------------- | --------------------- | ----------------------- | ------------------- |
| Dependencies         | Spring (http client and jdbc), Jackson, Postgres (optional)       | Scala, Akka, Jackson  | Scala, Akka             | None                |
| Cursor Management    | In-Memory / Persistent (Postgres)                                 | In-Memory             | Persistent (Dynamo)     |                     |
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

## Cursor persistence

Offsets for each partition can be stored in a postgres database by configuring a different `CursorManager`.

```java
final HikariConfig hikariConfig = new HikariConfig();
hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/local_nakadi_cursor_db");
hikariConfig.setUsername("postgres");
hikariConfig.setPassword("postgres");
final DataSource dataSource = new HikariDataSource(hikariConfig);
final CursorManager cursorManager = new PersistentCursorManager(dataSource);
```

## Using the managed high-level api

Using the high-level api works very similar to the low-level api. You have to use a different `CursorManager`, create a `Subscription` and then use this subscription to start streaming of event.

```java
final ManagedCursorManager cursorManager = new ManagedCursorManager(baseUri, requestFactory, objectMapper);
...
final Subscription subscription = nakadiClient.subscribe("fahrschein-demo2", eventName, "fahrschein-demo-sales-order-placed");
nakadiClient.listen(subscription, SalesOrderPlaced.class, listener, streamParameters);

```

## Using another ClientHttpRequestFactory

This library is currently only tested and fully working with `SimpleClientHttpRequestFactory`. Please note that `HttpComponentsClientHttpRequestFactory` tries to consume the remaining stream on closing and so might block until the configured `streamTimeout` during reconnection.

## Handling data binding problems

You might want to ignore events that could not be mapped to your domain objects by Jackson, instead of having these events block all further processing. To achieve this you can override the `onMappingException` method of `Listener` and handle the `JsonMappingException` yourself.

## Getting help

If you have questions, concerns, bug reports, etc, please file an issue in this repository's issue tracker.

## Getting involved

To contribute, simply make a pull request and add a brief description (1-2 sentences) of your addition or change.
For more details check the [contribution guidelines](CONTRIBUTING.md).
