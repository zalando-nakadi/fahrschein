package org.zalando.fahrschein.http.jdk11;

import java.net.http.HttpRequest;
import java.util.function.Function;

public interface HttpRequestBuilderAdapter extends Function<HttpRequest.Builder, HttpRequest.Builder> {
}
