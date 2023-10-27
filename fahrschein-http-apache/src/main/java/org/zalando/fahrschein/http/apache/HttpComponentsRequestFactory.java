package org.zalando.fahrschein.http.apache;


import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.io.IOException;
import java.net.URI;

/**
 * {@link RequestFactory} implementation that
 * uses <a href="http://hc.apache.org/httpcomponents-client-ga/">Apache HttpComponents
 * HttpClient</a> to create requests.
 *
 * <p>Allows to use a pre-configured {@link HttpClient} instance -
 * potentially with authentication, HTTP connection pooling, etc.
 *
 * <p><b>NOTE:</b> Requires Apache HttpComponents 4.3 or higher, as of Spring 4.0.
 *
 * @author Oleg Kalnichevski
 * @author Arjen Poutsma
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @author Joern Horstmann
 */
public class HttpComponentsRequestFactory implements RequestFactory {

    private final HttpClient httpClient;
    private final ContentEncoding contentEncoding;

    /**
     * Create a new instance of the {@code HttpComponentsRequestFactory}
     * with the given {@link HttpClient} instance.
     * @param httpClient the HttpClient instance to use for this request factory
     * @param contentEncoding content encoding for request payloads.
     */
    public HttpComponentsRequestFactory(HttpClient httpClient, ContentEncoding contentEncoding) {
        this.contentEncoding = contentEncoding;
        if (httpClient == null) {
            throw new IllegalArgumentException("HttpClient must not be null");
        }
        this.httpClient = httpClient;
    }

    @Override
    public Request createRequest(URI uri, String httpMethod) throws IOException {
        final HttpUriRequest httpRequest = createHttpUriRequest(httpMethod, uri);

        return new HttpComponentsRequest(httpClient, httpRequest, contentEncoding);
    }

    /**
     * Create a Commons HttpMethodBase object for the given HTTP method and URI specification.
     * @param method the HTTP method
     * @param uri the URI
     * @return the Commons HttpMethodBase object
     */
    private static HttpUriRequest createHttpUriRequest(String method, URI uri) {
        switch (method) {
            case "GET":
                return new HttpGet(uri);
            case "HEAD":
                return new HttpHead(uri);
            case "POST":
                return new HttpPost(uri);
            case "PUT":
                return new HttpPut(uri);
            case "PATCH":
                return new HttpPatch(uri);
            case "DELETE":
                return new HttpDelete(uri);
            case "OPTIONS":
                return new HttpOptions(uri);
            case "TRACE":
                return new HttpTrace(uri);
            default:
                throw new IllegalArgumentException("Invalid HTTP method: " + method);
        }
    }

}
