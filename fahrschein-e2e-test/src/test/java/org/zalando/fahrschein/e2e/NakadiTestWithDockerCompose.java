package org.zalando.fahrschein.e2e;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;

public abstract class NakadiTestWithDockerCompose {

    private static final boolean COMPOSE_PROVIDED = System.getProperty("COMPOSE_PROVIDED") != null;
    private static final Logger LOG = LoggerFactory.getLogger("docker-compose");
    private static final Consumer<OutputFrame> LOG_CONSUMER = new Slf4jLogConsumer(LOG);

    public static DockerComposeContainer<?> compose;

    private static HttpClient httpClient = HttpClient.newHttpClient();

    static {
        if (!COMPOSE_PROVIDED) {
            compose = COMPOSE_PROVIDED ? null :
                    new DockerComposeContainer(new File(NakadiTestWithDockerCompose.class.getResource("/docker-compose.yaml").getPath()))
                            .withExposedService("nakadi_postgres_1", 5432)
                            .withExposedService("zookeeper_1", 2181)
                            .withExposedService("kafka_1", 9092)
                            .withExposedService("nakadi_1", 8080,
                                    Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(180)))
                            .withLogConsumer("nakadi_1", LOG_CONSUMER);
            compose.start();
        }
    }

    protected URI getNakadiUrl() {
        return URI.create(COMPOSE_PROVIDED ? "http://localhost:8080" :
                format("http://%s:%d", compose.getServiceHost("nakadi_1", 8080),
                        compose.getServicePort("nakadi_1", 8080)));
    }

    protected void createEventTypes(String baseDir) throws IOException {
        URI nakadiUri = getNakadiUrl().resolve("/event-types");
        Path dir = Paths.get(this.getClass().getResource(baseDir).getPath());
        try (Stream<Path> stream = Files.list(dir)) {
            stream
                    .filter(file -> !Files.isDirectory(file))
                    .forEach((Path file) -> {
                        try {
                            postJson(HttpRequest.newBuilder()
                                    .header("Content-Type", "application/json")
                                    .uri(nakadiUri)
                                    .POST(HttpRequest.BodyPublishers.ofFile(file)).build());
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private String f(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String postJson(HttpRequest r) throws IOException, InterruptedException {
        return httpClient.send(r, HttpResponse.BodyHandlers.ofString()).body();
    }

}
