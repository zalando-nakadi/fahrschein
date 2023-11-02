package org.zalando.fahrschein.e2e;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;

public abstract class NakadiTestWithDockerCompose {

    private static final boolean COMPOSE_PROVIDED = Boolean.parseBoolean(System.getProperty("COMPOSE_PROVIDED"));
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
                            .withExposedService("nakadi_1", 8080,
                                    Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(180)))
                            .withLogConsumer("nakadi_1", LOG_CONSUMER);
            compose.start();
        }
    }

    protected URI getNakadiUrl() {
        return URI.create(COMPOSE_PROVIDED ? "http://localhost:8080" :
                format(Locale.ENGLISH, "http://%s:%d", compose.getServiceHost("nakadi_1", 8080),
                        compose.getServicePort("nakadi_1", 8080)));
    }

    /**
     * @param suffix appended to the end of the event type names, to ensure tests are independent from each other.
     * Placeholder must be present in the schema definition file, e.g.: "name": "fahrschein.e2e-test.ordernumber${SUFFIX}"
     */
    protected void createEventTypes(String baseDir, String suffix) throws IOException {
        URI nakadiUri = getNakadiUrl().resolve("/event-types");
        Path dir = Paths.get(this.getClass().getResource(baseDir).getPath());
        try (Stream<Path> stream = Files.list(dir)) {
            stream
                    .filter(file -> !Files.isDirectory(file))
                    .forEach((Path file) -> {
                        try {
                            String schema = f(file).replaceAll("\\$\\{SUFFIX\\}", suffix);
                            postJson(new StringEntity(schema), nakadiUri);
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
        return httpClient.execute(post, response -> {
            if (response.getCode() >= 300) {
                EntityUtils.consume(response.getEntity());
                throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
            } else {
                return response.getEntity() == null ? null : EntityUtils.toString(response.getEntity());
            }
        });
    }

}
