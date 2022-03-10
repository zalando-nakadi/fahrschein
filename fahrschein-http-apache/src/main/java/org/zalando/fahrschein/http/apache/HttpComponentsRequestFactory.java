package org.zalando.fahrschein.http.apache;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
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
    private boolean contentCompression = true;

    /**
     * Create a new instance of the {@code HttpComponentsRequestFactory}
     * with the given {@link HttpClient} instance.
     * @param httpClient the HttpClient instance to use for this request factory
     */
    public HttpComponentsRequestFactory(HttpClient httpClient) {
        if (httpClient == null) {
            throw new IllegalArgumentException("HttpClient must not be null");
        }
        this.httpClient = httpClient;
    }

    @Override
    public void disableContentCompression() {
        this.contentCompression = false;
    }

    @Override
    public Request createRequest(URI uri, String httpMethod) throws IOException {
        final HttpUriRequest httpRequest = createHttpUriRequest(httpMethod, uri);

        return new HttpComponentsRequest(httpClient, httpRequest, contentCompression);
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

    /**
     * An alternative to {@link org.apache.http.client.methods.HttpDelete} that
     * extends {@link org.apache.http.client.methods.HttpEntityEnclosingRequestBase}
     * rather than {@link org.apache.http.client.methods.HttpRequestBase} and
     * hence allows HTTP delete with a request body. For use with the RestTemplate
     * exchange methods which allow the combination of HTTP DELETE with entity.
     * @since 4.1.2
     */
    static class HttpDelete extends HttpEntityEnclosingRequestBase {

        HttpDelete(URI uri) {
            super();
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return "DELETE";
        }
    }

}
