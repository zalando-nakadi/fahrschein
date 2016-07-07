package org.zalando.fahrschein;

import org.zalando.problem.Problem;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@SuppressWarnings("serial")
public class IOProblem extends IOException implements Problem {

    private final URI type;
    private final String title;
    private final Response.StatusType status;
    @Nullable
    private final String detail;
    @Nullable
    private final URI instance;

    public IOProblem(final URI type, final String title, final Response.StatusType status, @Nullable final String detail, @Nullable final URI instance) {
        super(formatMessage(type, title, status.getStatusCode()));
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

    public IOProblem(final URI type, final String title, final Response.StatusType status, @Nullable final String detail) {
        this(type, title, status, detail, null);
    }

    public IOProblem(final URI type, final String title, final Response.StatusType status) {
        this(type, title, status, null, null);
    }

    private static String formatMessage(final URI type, final String title, final int status) {
        return String.format("Problem [%s]: Status %d [%s]", type, status, title);
    }

    @Override
    public URI getType() {
        return type;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Response.StatusType getStatus() {
        return status;
    }

    @Override
    public Optional<String> getDetail() {
        return Optional.ofNullable(detail);
    }

    @Override
    public Optional<URI> getInstance() {
        return Optional.ofNullable(instance);
    }
}
