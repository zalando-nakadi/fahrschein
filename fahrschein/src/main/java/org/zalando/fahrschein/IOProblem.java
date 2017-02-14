package org.zalando.fahrschein;


import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;

@SuppressWarnings("serial")
public class IOProblem extends IOException {

    private final URI type;
    private final String title;
    private final int statusCode;
    @Nullable
    private final String detail;
    @Nullable
    private final URI instance;

    public IOProblem(final URI type, final String title, final int statusCode, @Nullable final String detail, @Nullable final URI instance) {
        super(formatMessage(type, title, statusCode, detail));
        this.type = type;
        this.title = title;
        this.statusCode = statusCode;
        this.detail = detail;
        this.instance = instance;
    }

    public IOProblem(final URI type, final String title, final int statusCode, @Nullable final String detail) {
        this(type, title, statusCode, detail, null);
    }

    public IOProblem(final URI type, final String title, final int statusCode) {
        this(type, title, statusCode, null, null);
    }

    private static String formatMessage(final URI type, final String title, final int status, @Nullable final String detail) {
        return String.format("Problem [%s] with status [%d]: [%s] [%s]", type, status, title, detail == null ? "" : detail);
    }

    public URI getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Nullable
    public String getDetail() {
        return detail;
    }

    @Nullable
    public URI getInstance() {
        return instance;
    }

}
