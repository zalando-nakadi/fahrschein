package org.zalando.fahrschein.http.api.tracing;


public interface TracingInterceptor {

    void injectTrace(String eventName, int size);

    void recordError(Throwable t);
}
