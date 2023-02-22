package org.zalando.fahrschein.http.api;

/**
 * Interface that is offering a way to add additional features around the {@code NakadiClient}
 */
public interface RequestHandler {

    /**
     * This method is called by the {@code NakadiClient} before
     * a request is going to be executed.
     * @param request that is going to be executed after the invocation
     */
    void beforeExecute(Request request);

    /**
     * This method is called by the {@code NakadiClient} after
     * a request got executed.
     * @param request that got send to Nakadi
     * @param response that we received from Nakadi
     */
    void afterExecute(Request request, Response response);

    /**
     * This method is called by the {@code NakadiClient} in case
     * an exception got thrown while sending the request.
     * The exception itself is rethrown by the client.
     * @param request that should be sent to Nakadi
     * @param t Exception that got thrown
     */
    void onError(Request request, Throwable t);
}
