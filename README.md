# Fahrschein

> You need a fahrschein to use the [nakadi](https://github.com/zalando/nakadi) event bus.

[![Fahrschein](docs/Bundesarchiv_Bild_183-1990-0104-025,_BVG-Fahrscheine.jpg)](https://commons.wikimedia.org/wiki/File:Bundesarchiv_Bild_183-1990-0104-025,_BVG-Fahrscheine.jpg)

*Image Source: Deutsches Bundesarchiv, Photographer: Ralph Hirschberger, Image License: [CC BY-SA 3.0 DE](https://creativecommons.org/licenses/by-sa/3.0/de/deed.en)*

[![Build Status](https://travis-ci.org/zalando-nakadi/fahrschein.svg?branch=master)](https://travis-ci.org/zalando-nakadi/fahrschein)
[![Release](https://img.shields.io/github/release/zalando-nakadi/fahrschein.svg)](https://github.com/zalando-nakadi/fahrschein/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/fahrschein.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/fahrschein)

## Features

 - Consistent error handling
 - Stream-based parsing
    - Optimized utf-8 decoding by [Jackson](https://github.com/FasterXML/jackson)
    - No unnecessary buffering or line-based processing, causing less garbage
    - Less garbage and higher performance
    - No required base classes for events
 - Support for both high-level (subscription) and low-level APIs
 - Pluggable HTTP client implementations

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

// Configure client, defaults to using the high level api with ManagedCursorManger and SimpleRequestFactory
final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
        .withAccessTokenProvider(new ZignAccessTokenProvider())
        .build();

// Create subscription using the high level api
Subscription subscription = nakadiClient.subscription(applicationName, eventName).subscribe();

// Start streaming, the listen call will block and automatically reconnect on IOException
nakadiClient.stream(subscription)
        .listen(SalesOrderPlaced.class, listener);

```

See [`Main.java`](fahrschein-example/src/main/java/org/zalando/fahrschein/example/Main.java) for an executable version of the above code.

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

## Using Nakadi's Low-level API

*Please do not use the Low-level API, as it is deprecated by Nakadi.*

The Low-level API requires local persistence of partition offsets.
There are currently three persistent `CursorManager` implementations: InMemory, Postgres and Redis.

!!! warning
    Postgres and Redis cursor managers are DEPRECATED and will be
    removed in an upcoming version of Fahrschein.

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

### Fahrschein-JDBC Schema migration

Fahrschein-JDBC provides Flyway schema migrations in the `fahrschein-db` resource folder. You can
point Flyway to it using the [`resources`](https://flywaydb.org/documentation/configuration/parameters/locations) configuration parameter.

## Using multiple partitions and multiple consumers

With the `PartitionManager` api it is possible to coordinate between multiple nodes of one application, so that only one node is consuming events from a partition at the same time.

Partitions are locked by one node for a certain time. This requires that every node has an unique name or other identifier.

```java
@Scheduled(fixedDelay = 60*1000L)
public void readSalesOrderPlacedEvents() throws IOException {
    final String lockedBy = ... // host name or another unique identifier for this node
    final List<Partition> partitions = nakadiClient.getPartitions(eventName);
    final Optional<Lock> optionalLock = partitionManager.lockPartitions(eventName, partitions, lockedBy);

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

 - `IOException` and its subclasses are treated as temporary failures. Library will automatically recover from this 
 type of failure by retrying processing attempt after time interval specified by the `BackoffStrategy`.
 **Special case**: if an `IOException` happens during opening the initial connection, this is treated as configuration 
 problem (wrong host name or missing scopes). In this case processing will be aborted and exception will be re-thrown. 
 - If `listener` throws `RuntimeException` streaming of events will be aborted. User is responsible for handling these exceptions.
 Library code itself will not throw `RuntimeException`s.
 - Exceptions in other client methods are not automatically retried

## Stopping and resuming streams

The stream implementation gracefully handles thread interruption, so it is possible to stop a running thread and resume consuming events by re-submitting the `Runnable`:

```java
final ExecutorService executorService = Executors.newSingleThreadExecutor();

final Runnable runnable = nakadiClient.stream(SALES_ORDER_SERVICE_ORDER_PLACED)
        .runnable(SalesOrderPlaced.class, listener)
        .unchecked();

// start consuming events
final Future<?> future = executorService.submit(runnable);

// stop consuming events
future.cancel(true);

// resume consuming events
final Future<?> future2 = executorService.submit(runnable);
```

### Handling data binding problems

You might want to ignore events that could not be mapped to your domain objects by Jackson, instead of having these events block all further processing.
To achieve this you can implement the `onMappingException` method of the `ErrorHandler` interface handle the `JsonMappingException` yourself.

```java
nakadiClient.stream(eventName)
        .withErrorHandler(e -> {...})
        .listen(SalesOrderPlaced.class, listener);
```

## `RequestFactory` implementations

Fahrschein uses it's own http abstraction which is very similar to spring framework's [`ClientHttpRequestFactory`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/http/client/ClientHttpRequestFactory.html) interface. By default it uses the `SimpleRequestFactory` which uses a `HttpURLConnection` internally and has no further dependencies.

There is also a version using apache http components named `HttpComponentsRequestFactory` in the `fahrschein-http-apache` artifact.

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>fahrschein-http-apache</artifactId>
    <version>${fahrschein.version}</version>
</dependency>
```

The apache version is useful when you want more control about the number of parallel connections in total or per host. The following example shows how to use a customized `HttpClient`:

```java
final RequestConfig config = RequestConfig.custom().setSocketTimeout(readTimeout)
                                                   .setConnectTimeout(connectTimeout)
                                                   .setConnectionRequestTimeout(connectTimeout)
                                                   .build();

final CloseableHttpClient httpClient = HttpClients.custom()
                                                  .setConnectionTimeToLive(readTimeout, TimeUnit.MILLISECONDS)
                                                  .disableAutomaticRetries()
                                                  .setDefaultRequestConfig(config)
                                                  .disableRedirectHandling()
                                                  .setMaxConnTotal(8)
                                                  .setMaxConnPerRoute(2)
                                                  .build();

final RequestFactory requestFactory = new HttpComponentsRequestFactory(httpClient);

final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
        .withRequestFactory(requestFactory)
        .withAccessTokenProvider(new ZignAccessTokenProvider())
        .build();
```

It is also possible to adapt other implementations from spring framework by wrapping them into `SpringRequestFactory`, contained in the `fahrschein-http-spring` artifact.

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>fahrschein-http-spring</artifactId>
    <version>${fahrschein.version}</version>
</dependency>
```

Example using OkHttp 3.x:

```java

final ClientHttpRequestFactory clientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory();
final RequestFactory requestFactory = new SpringRequestFactory(clientHttpRequestFactory);

final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI)
        .withRequestFactory(requestFactory)
        .withAccessTokenProvider(new ZignAccessTokenProvider())
        .build();

```

**Note:** The implementations from spring framework don't handle closing of streams as expected. They will try to consume remaining data, which will usually time out when nakadi does not receive a commit.


## Fahrschein compared to other nakadi client libraries

|                      | Fahrschein                                                        | Nakadi-Klients        | Reactive-Nakadi         | Straw               |
| -------------------- | ----------------------------------------------------------------- | --------------------- | ----------------------- | ------------------- |
| Dependencies         | Jackson                                                           | Scala, Akka, Jackson  | Scala, Akka             | None                |
| Cursor Management    | In-Memory / Persistent (Postgres or Redis)                        | In-Memory             | Persistent (Dynamo)     |                     |
| Partition Management | In-Memory / Persistent (Postgres)                                 |                       | Persistent (Dynamo) (?) |                     |
| Error Handling       | Automatic reconnect with exponential backoff                      | Automatic reconnect   | (?)                     | No error handling   |

## Getting help

If you have questions, concerns, bug reports, etc, please file an issue in this repository's issue tracker.

## Local development

For local development, Fahrschein requires:

* A local installation of JDK8
* Any recent version of Maven
* A local Docker installation for running integration tests

When developing, make sure to run unit and integration tests with `mvn verify`.

## Getting involved

Check the [contribution guidelines](CONTRIBUTING.md) if you want to get involved in Fahrschein development.
