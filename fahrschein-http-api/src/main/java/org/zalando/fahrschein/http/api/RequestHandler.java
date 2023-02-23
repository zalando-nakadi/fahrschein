package org.zalando.fahrschein.http.api;

import java.util.List;

/**
 * Interface that is offering a way to add additional features around the {@code NakadiClient}.
 *
 * As the methods of this class are invoked on every publish request, we shouldn't
 *
 * - do heavy computations as it might affect performance significantly
 * - do modification of requests
 */
public interface RequestHandler {

    /**
     * This method is called by the {@code NakadiClient} when a request towards Nakadi is going to be sent.
     * For example, it can be used to record things before that.
     *
     * @param eventName that is used for the published events
     * @param events that are published
     * @param <T> type of events that we publish
     */
    <T> void onPublish(String eventName, List<T> events);

    /**
     * This method is invoked after the publishing of events has happened.
     * It can be used to evaluate the given response or others like closing a trace.
     *
     * @param response
     */
    void afterPublish(Response response);

    /**
     * Invoked when publishing of events failed.
     *
     * @param events that failed to be published
     * @param t the throwable we experienced while publishing
     * @param <T> type of events that we publish
     */
    <T> void onError(List<T> events, Throwable t);

}
