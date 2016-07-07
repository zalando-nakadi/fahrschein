package org.zalando.fahrschein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Optional;

public class ProblemObject {
    private final URI type;
    private final String title;
    private final int status;
    private final Optional<String> detail;
    private final Optional<URI> instance;

    @JsonCreator
    public ProblemObject(@JsonProperty("type") URI type, @JsonProperty("title") String title, @JsonProperty("status") int status, @JsonProperty("detail") Optional<String> detail, @JsonProperty("instance") Optional<URI> instance) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

    public URI getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public Optional<String> getDetail() {
        return detail;
    }

    public Optional<URI> getInstance() {
        return instance;
    }
}
