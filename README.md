# Fahrschein

> You need a fahrschein to use the [nakadi](https://github.com/zalando/nakadi) event bus.

[![Fahrschein](docs/Bundesarchiv_Bild_183-1990-0104-025,_BVG-Fahrscheine.jpg)](https://commons.wikimedia.org/wiki/File:Bundesarchiv_Bild_183-1990-0104-025,_BVG-Fahrscheine.jpg)

*Image Source: Deutsches Bundesarchiv, Photographer: Ralph Hirschberger, Image License: [CC BY-SA 3.0 DE](https://creativecommons.org/licenses/by-sa/3.0/de/deed.en)*

![Build Status](https://github.com/zalando-nakadi/fahrschein/actions/workflows/ci.yml/badge.svg)
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
 - Gzip encoding support for publishing and consuming events
 - ZStandard compression support for publishing events

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

// Configure client, defaults to using the high level api with ManagedCursorManger, 
// using the SimpleRequestFactory without compression
final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI, new SimpleRequestFactory(ContentEncoding.IDENTITY))
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

// NakadiClient can be configured to start reading from the oldest available offset in each partition
nakadiClient.stream(eventName).readFromBegin(partitions);

// Or from the newest available offset, but this is the same as the default
nakadiClient.stream(eventName).readFromNewestAvailableOffset(partitions);

// Or (for a persistent cursor manager) we can start reading from the last offset that we processed if it's still available, and from the oldest available offset otherwise
nakadiClient.stream(eventName).skipUnavailableOffsets(partitions);
```

## Using Nakadi's Low-level API

*Please do not use the Low-level API, as it is deprecated by Nakadi.*

The Low-level API requires local persistence of partition offsets.
There is one `CursorManager` implementation left: InMemory.
Postgres and Redis cursor managers have been DEPRECATED and removed in version 0.22.0 of Fahrschein.

```java
final CursorManager cursorManager = new InMemoryCursorManager();

final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI, new SimpleRequestFactory(ContentEncoding.IDENTITY))
        .withAccessTokenProvider(new ZignAccessTokenProvider())
        .withCursorManager(cursorManager)
        .build();

nakadiClient.stream(eventName)
        .listen(SalesOrderPlaced.class, listener);
```

## Using multiple partitions and multiple consumers

With the `PartitionManager` api it is possible to coordinate between multiple nodes of one application, so that only one node is consuming events from a partition at the same time.

Partitions are locked by one node for a certain time. This requires that every node has a unique name or other identifier.

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

## Backoff Strategies

Fahrschein supports different exponential backoff strategies when streaming events. All backoff values are configurable when passing your own instance in `StreamBuilder#withBackoffStrategy(BackoffStrategy)`.

* `ExponentialBackoffStrategy` - Base implementation for exponential backoff without jitter. Initial delay is 500ms, backoff factor 1.5, maximum delay 10min, with no limit on the maximum number of retries.
* `EqualJitterBackoffStrategy` (default) - extends `ExponentialBackoffStrategy` with the same defaults. For each delay it takes half of the delay value and adds the other half multiplied by a random factor [0..1).
* `FullJitterBackoffStrategy` - extends `ExponentialBackoffStrategy` with the same defaults and multiplies each delay by a random factor [0..1).

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


There is a version using java.net HttpClient (JDK11+) named `JavaNetRequestFactory` in the `fahrschein-http-jdk11` artifact.

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>fahrschein-http-jdk11</artifactId>
    <version>${fahrschein.version}</version>
</dependency>
```

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
                                                  .setMaxConnTotal(20)
                                                  .setMaxConnPerRoute(20)
                                                  .build();

final RequestFactory requestFactory = new HttpComponentsRequestFactory(httpClient, ContentEncoding.GZIP);

final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI, requestFactory)
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
final RequestFactory requestFactory = new SpringRequestFactory(clientHttpRequestFactory, ContentEncoding.GZIP);

final NakadiClient nakadiClient = NakadiClient.builder(NAKADI_URI, requestFactory)
        .withAccessTokenProvider(new ZignAccessTokenProvider())
        .build();

```

**Note:** The implementations from the Spring framework don't handle closing of streams as expected. They will try to consume remaining data, which will usually time out when nakadi does not receive a commit.

**Note:** Regarding sizing and reuse of HTTP client connection pools, make sure to have a connection pool 
size bigger than the number of subscriptions you're making, because subscriptions use long-polling to
retrieve events, each effectively blocking one connection.

## Dependency compatibility

Although Fahrschein is using fixed dependency versions, it is integration-tested against the following dependency matrix. We will inform in the release notes in case we bump the compatibility baseline. 

| Dependency | Baseline | Latest |
| ---------- | -------- | ------ |
| Jackson | 2.8.0 | 2.13.+ |
| Spring Core | 4.3.0.RELEASE | 5.3.+ |
| okHttp | 3.3.0 | 4.9.+ |
| Apache HttpClient | 4.3 | 4.5.+ |

## Content-Compression

Fahrschein handles content compression transparently to the API consumer, and mostly independently of the actual HTTP
client implementation. Since version `0.20.0` it can be enabled to both compress HTTP POST bodies when event
publishing, and requesting payload compression from Nakadi when consuming events.

### Consuming

For event consumption the underlying HTTP client implementations send `Accept-Encoding` headers, indicating their supported compression algorithms.
At the time of writing, all tested client implementations default to `gzip` compression. If this is undesired, wrap your 
RequestFactory into a `IdentityAcceptEncodingRequestFactory`, which sets the `Accept-Encoding` header to `identity`.

### Publishing

For event publishing, the `Request` body can also get compressed by Fahrschein, if enabled when building the RequestFactory.
For this, you need to pass either `ContentEncoding.GZIP`, `ContentEncoding.ZSTD`, or if compression is undesired, pass `ContentEncoding.IDENTITY`.
Zstandard compression was added in version `0.21.0`.

## Fahrschein compared to other Nakadi client libraries

|                      | Fahrschein | [nakadi-java](https://github.com/dehora/nakadi-java) |
| -------------------- | ---------- | ---------------------------------------------------- |
| Dependencies         | Jackson    | gson, okhttp3, RxJava |
| Low-level API streaming | yes |  yes |
| Subscription API streaming | yes |  yes |
| Compression: consuming | gzip (enabled by default) | gzip (enabled by default) |
| Compression: publishing | gzip, zstd | none |
| Error Handling | Automatic retry with exponential backoff | Automatic retry with exponential backoff |
| OpenTracing | yes | yes |
| Metrics Collection | yes | yes |
| Access to Event Metadata | no | yes |
| Event Type manipulation | no | yes |

## Getting help

If you have questions, concerns, bug reports, etc, please file an issue in this repository's issue tracker.

## Local development

Fahrschein is a gradle-based project. For local development, Fahrschein requires:

* A local installation of JDK11
* A local Docker installation for running integration tests

When developing, make sure to run unit and integration tests with `./gradlew test`.

### Understanding the build

We use Gradle [convention plugins](https://docs.gradle.org/current/samples/sample_convention_plugins.html) to share common build logic across multiple subprojects. These are [fahrschein.java-conventions.gradle](./buildSrc/src/main/groovy/fahrschein.java-conventions.gradle) for common java properties, and [fahrschein.maven-publishing-conventions.gradle](./buildSrc/src/main/groovy/fahrschein.maven-publishing-conventions.gradle) for subprojects that are released as maven artefacts.

### Bumping dependency versions

Most dependencies are defined on a per-subproject level, only the versions for the most-used shared dependencies are controlled centrally, in the [gradle.properties](./gradle.properties) file. This also allows you testing your build with a different version by specifying the property on the command-line.

```sh
./gradlew check -Pjackson.version=2.9.0
```

The [integration tests](.github/workflows/ci.yaml) include running the build with our supported baseline dependency as well as the latest micro release of Jackson, Apache HttpClient and Spring. Please update the section in the README when bumping dependency baselines, and add this to the release notes.


### Unit Tests and Code Coverage

Fahrschein automatically generates test coverage reports via its `JaCoCo` build integration. For example, you can check code coverage for a single subproject:

```sh
./gradlew fahrschein-http-api:check
open fahrschein-http-api/build/reports/jacoco/test/html/index.html
```

Alternatively, run the full build and check the aggregated coverage report:

```sh
./gradlew check -Pe2e.skip
open build/reports/jacoco/testCodeCoverageReport/html/index.html
```

### End-to-End tests

The `fahrschein-e2e-test` module has end-to-end tests using `docker-compose`. If you wish to leave the Docker containers running between tests, you can also bring up docker-compose manually using its [docker-compose.yaml](fahrschein-e2e-test/src/test/resources/docker-compose.yaml), before running the tests. In either case, you will need the port `8080` to be available for Nakadi to run.

```sh
docker-compose -f fahrschein-e2e-test/src/test/resources/docker-compose.yaml up -d
./gradlew :fahrschein-e2e:check -Pe2e.composeProvided
```

If you want to skip end-to-end tests completely, run 

```sh
./gradlew check -Pe2e.skip
```

### CVE scanning

The project integrates CVE scanning to check for vulnerable dependencies. In case of build failure, this can be caused by a high-risk vulnerability in a dependency being identified. You can run the reporting locally:

```
./gradlew :dependencyCheckAggregate
open build/reports/dependency-check-report.html
```

### Releasing

Fahrschein uses Github Workflows to [build](.github/workflows/ci.yaml) and [publish releases](.github/workflows/maven-publish.yaml). This happens automatically whenever a new release is created in Github. After creating a release, please bump the `project.version` property in [gradle.properties](./gradle.properties).

If needed, you can preview the signed release artifacts in your local maven repository.

```sh
env "GPG_KEY=$(cat ~/.gnupg/private-key.pgp)" \
"GPG_PASSPHRASE=$(read -s password ; echo $password)" \
./gradlew publishToMavenLocal
```



## Getting involved

Check the [contribution guidelines](CONTRIBUTING.md) if you want to get involved in Fahrschein development.
