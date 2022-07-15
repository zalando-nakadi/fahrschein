package org.zalando.fahrschein.http.simple;

import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * {@link RequestFactory} implementation that uses standard JDK facilities.
 *
 * See original
 * <a href="https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/http/client/SimpleClientHttpRequestFactory.java">code from Spring Framework</a>
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Joern Horstmann
 * @see java.net.HttpURLConnection
 */
public final class SimpleRequestFactory implements RequestFactory {

    private static final int DEFAULT_CONNECT_TIMEOUT = 500;
    private static final int DEFAULT_READ_TIMEOUT = 60 * 1000;

    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private final ContentEncoding contentEncoding;

    public SimpleRequestFactory(ContentEncoding contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * Set the underlying URLConnection's connect timeout (in milliseconds).
     * A timeout value of 0 specifies an infinite timeout.
     * <p>Default is the system's default timeout.
     *
     * @see URLConnection#setConnectTimeout(int)
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Set the underlying URLConnection's read timeout (in milliseconds).
     * A timeout value of 0 specifies an infinite timeout.
     * <p>Default is the system's default timeout.
     *
     * @see URLConnection#setReadTimeout(int)
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    public Request createRequest(URI uri, String method) throws IOException {
        HttpURLConnection connection = openConnection(uri.toURL());
        prepareConnection(connection, method);

        return new SimpleBufferingRequest(connection, contentEncoding);
    }

    /**
     * Opens and returns a connection to the given URL.
     *
     * @param url  the URL to open a connection to
     * @return the opened connection
     * @throws IOException in case of I/O errors
     * @throws IllegalArgumentException in case {{@link java.net.URL#openConnection()}} does not lead to a HttpURLConnection
     */
    private HttpURLConnection openConnection(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IllegalArgumentException("Connection should be an HttpURLConnection");
        }
        return (HttpURLConnection) urlConnection;
    }

    /**
     * Template method for preparing the given {@link HttpURLConnection}.
     * <p>The default implementation prepares the connection for input and output, and sets the HTTP method.
     *
     * @param connection the connection to prepare
     * @param httpMethod the HTTP request method ({@code GET}, {@code POST}, etc.)
     * @throws IOException in case of I/O errors
     */
    private void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        if (this.connectTimeout >= 0) {
            connection.setConnectTimeout(this.connectTimeout);
        }
        if (this.readTimeout >= 0) {
            connection.setReadTimeout(this.readTimeout);
        }

        connection.setDoInput(true);

        if ("GET".equals(httpMethod)) {
            connection.setInstanceFollowRedirects(true);
        } else {
            connection.setInstanceFollowRedirects(false);
        }

        if ("POST".equals(httpMethod) || "PUT".equals(httpMethod) || "PATCH".equals(httpMethod) || "DELETE".equals(httpMethod)) {
            connection.setDoOutput(true);
        } else {
            connection.setDoOutput(false);
        }

        connection.setRequestMethod(httpMethod);
    }

}
