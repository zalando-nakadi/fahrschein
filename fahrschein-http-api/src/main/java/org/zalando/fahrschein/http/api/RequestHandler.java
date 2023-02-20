package org.zalando.fahrschein.http.api;

public interface RequestHandler {

    void beforeExecute(Request request);

    void afterExecute(Request request);

    void onError(Request request);
}
