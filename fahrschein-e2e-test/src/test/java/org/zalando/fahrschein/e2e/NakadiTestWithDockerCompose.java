package org.zalando.fahrschein.e2e;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;

public abstract class NakadiTestWithDockerCompose {

    private static final boolean COMPOSE_PROVIDED = System.getProperty("COMPOSE_PROVIDED") != null;
    private static final Logger LOG = LoggerFactory.getLogger("docker-compose");
    private static final Consumer<OutputFrame> LOG_CONSUMER = new Slf4jLogConsumer(LOG);

    public static DockerComposeContainer<?> compose;

    private static CloseableHttpClient httpClient = HttpClients.createDefault();

    static {
        if (!COMPOSE_PROVIDED) {
            compose = COMPOSE_PROVIDED ? null :
                    new DockerComposeContainer(new File(NakadiTestWithDockerCompose.class.getResource("/docker-compose.yaml").getPath()))
                            .withExposedService("nakadi_postgres_1", 5432)
                            .withExposedService("zookeeper_1", 2181)
                            .withExposedService("kafka_1", 9092)
                            .withExposedService("nakadi_1", 8080)
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
                            postJson(new StringEntity(f(file)), nakadiUri);
                        } catch (HttpResponseException e) {
                            if (e.getStatusCode() != 409) {
                                throw new RuntimeException(e);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private String f(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String postJson(HttpEntity e, URI uri) throws IOException {
        HttpPost post = new HttpPost(uri);
        post.setEntity(e);
        post.setHeader("Content-Type", "application/json");
        return httpClient.execute(post, new BasicResponseHandler());
    }

}
