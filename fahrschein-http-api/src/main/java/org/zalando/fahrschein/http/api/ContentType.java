package org.zalando.fahrschein.http.api;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ContentType {
    private static final Pattern PATTERN = Pattern.compile("([a-zA-Z0-9_+\\-]+)/([a-zA-Z0-9_+\\-]+)(?:[ \t]*;[ \t]*(.*))?");

    public static final String TEXT_PLAIN_VALUE = "text/plain";
    public static final String APPLICATION_JSON_VALUE = "application/json";
    public static final String APPLICATION_JSON_UTF8_VALUE = APPLICATION_JSON_VALUE + ";charset=UTF-8";
    public static final String APPLICATION_PROBLEM_JSON_VALUE = "application/problem+json";

    public static final ContentType TEXT_PLAIN = ContentType.valueOf(TEXT_PLAIN_VALUE);
    public static final ContentType APPLICATION_JSON = ContentType.valueOf(APPLICATION_JSON_VALUE);
    public static final ContentType APPLICATION_JSON_UTF8 = ContentType.valueOf(APPLICATION_JSON_UTF8_VALUE);
    public static final ContentType APPLICATION_PROBLEM_JSON= ContentType.valueOf(APPLICATION_PROBLEM_JSON_VALUE);


    private final String value;
    private final String type;
    private final String subtype;
    @Nullable
    private final String parameters;

    private ContentType(String value, String type, String subtype, String parameters) {
        this.value = value;
        this.type = type;
        this.subtype = subtype;
        this.parameters = parameters;
    }

    public static ContentType valueOf(String contentType) {
        final Matcher matcher = PATTERN.matcher(contentType);
        if (matcher.matches()) {
            return new ContentType(contentType, matcher.group(1), matcher.group(2), matcher.group(3));
        } else {
            throw new IllegalArgumentException("Invalid content type [" + contentType + "]");
        }
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ContentType && value.equals(((ContentType)o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
