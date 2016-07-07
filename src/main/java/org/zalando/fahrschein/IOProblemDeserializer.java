package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class IOProblemDeserializer extends JsonDeserializer<IOProblem>{

    @Override
    public IOProblem deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode rootNode = jp.getCodec().readTree(jp);
        final String type = rootNode.get("type").asText();
        final String title = rootNode.get("title").asText();
        final int status = rootNode.get("status").asInt();

        final JsonNode detailNode = rootNode.get("detail");
        final Optional<String> detail = ofNullable(detailNode != null ? detailNode.asText(null) : null);

        final JsonNode instanceNode = rootNode.get("instance");
        final String instance = instanceNode != null ? instanceNode.asText(null) : null;

        try {
            Optional<URI> maybeInstance = instance != null ? Optional.of(new URI(instance)) : empty();

            return new IOProblem(new URI(type), title, status, detail, maybeInstance);
        } catch (URISyntaxException e) {
            throw new IOException("Cannot deserialize IOProblem json", e);
        }
    }
}
