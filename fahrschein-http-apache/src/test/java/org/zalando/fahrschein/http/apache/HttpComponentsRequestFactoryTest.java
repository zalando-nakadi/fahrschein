package org.zalando.fahrschein.http.apache;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.Response;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class HttpComponentsRequestFactoryTest {

    static HttpServer server;
    static URI serverAddress;

    @BeforeClass
    public static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 1);
        serverAddress = URI.create("http://localhost:" + server.getAddress().getPort());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        server.setExecutor(executor);
        server.start();
    }

    @Captor
    public ArgumentCaptor<HttpExchange> exchangeCaptor;

    @Test
    public void testGzippedResponseBody() throws IOException {
        // given
        String expectedResponse = "{}";
        HttpHandler spy = Mockito.spy(new GzippedResponseContentHandler(expectedResponse));
        server.createContext("/gzipped", spy);

        // when
        final RequestFactory f = defaultRequestFactory();
        f.disableContentCompression();
        Request r = f.createRequest(serverAddress.resolve("/gzipped"), "GET");
        Response executed = r.execute();
        String actualResponse = readStream(executed.getBody());

        // then
        Mockito.verify(spy).handle(exchangeCaptor.capture());
        HttpExchange capturedArgument = exchangeCaptor.getValue();
        assertThat("accept-encoding header", capturedArgument.getRequestHeaders().get("accept-encoding"), equalTo(Arrays.asList("gzip,deflate")));
        assertThat("no content-encoding header", capturedArgument.getRequestHeaders().get("content-encoding"), nullValue());
        assertEquals(URI.create("/gzipped"), capturedArgument.getRequestURI());
        assertEquals(expectedResponse, actualResponse);
    }

    @Test(expected = SocketTimeoutException.class)
    public void testTimeout() throws IOException {
        // given
        server.createContext("/timeout", exchange -> {
            try {
                Thread.sleep(10l);
                exchange.sendResponseHeaders(201, 0);
            } catch (InterruptedException e) { }
        });

        // when
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1).build();
        final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        RequestFactory f = new HttpComponentsRequestFactory(httpClient);
        Request r = f.createRequest(serverAddress.resolve("/timeout"), "GET");
        r.execute();
    }

    @Test
    public void testGzippedRequestBody() throws IOException {
        // given
        String requestBody = "{}";
        String responseBody = "{}";
        SimpleRequestResponseContentHandler spy = Mockito.spy(new SimpleRequestResponseContentHandler(responseBody));
        server.createContext("/gzipped-post", spy);

        // when
        CloseableHttpClient c = HttpClients.createDefault();
        HttpComponentsRequestFactory f = new HttpComponentsRequestFactory(c);

        Request r = f.createRequest(serverAddress.resolve("/gzipped-post"), "POST");
        r.getHeaders().setContentType(ContentType.APPLICATION_JSON);
        try (final OutputStream body = r.getBody()) {
            body.write(requestBody.getBytes());
        }
        Response executed = r.execute();
        String actualResponse = readStream(executed.getBody());

        // then
        Mockito.verify(spy).handle(exchangeCaptor.capture());
        HttpExchange capturedArgument = exchangeCaptor.getValue();
        assertEquals("POST", capturedArgument.getRequestMethod());
        assertEquals(URI.create("/gzipped-post"), capturedArgument.getRequestURI());
        assertThat("content-encoding header", capturedArgument.getRequestHeaders().get("content-encoding"), equalTo(Arrays.asList("gzip")));
        assertEquals(requestBody, spy.getRequestBody());
        assertEquals(responseBody, actualResponse);
    }

    private RequestFactory defaultRequestFactory() {
        return new HttpComponentsRequestFactory(HttpClients.createDefault());
    }

    static String readStream(InputStream stream) throws IOException {
        String res = new BufferedReader(
                new InputStreamReader(stream, UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        stream.close();
        return res;
    }

    private static class SimpleRequestResponseContentHandler implements HttpHandler {

        private String requestBody;
        private final String responseBody;

        SimpleRequestResponseContentHandler(String responseBody) {
            this.responseBody = responseBody;
        }

        public String getRequestBody() {
            return requestBody;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            try {
                if (exchange.getRequestHeaders().containsKey("Content-Encoding") && exchange.getRequestHeaders().get("Content-Encoding").contains("gzip")) {
                    requestBody = readStream(new GZIPInputStream(exchange.getRequestBody()));
                } else {
                    requestBody = readStream(exchange.getRequestBody());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] bytes = responseBody.getBytes(UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write(bytes);
            responseBody.close();
        }
    }

    private static class GzippedResponseContentHandler implements HttpHandler {

        private final byte[] rawResponse;

        GzippedResponseContentHandler(String response) throws IOException {
            byte[] stringResponse = response.getBytes(UTF_8);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);
            zipStream.write(stringResponse);
            zipStream.close();
            this.rawResponse = byteStream.toByteArray();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
            exchange.sendResponseHeaders(200, rawResponse.length);
            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write(rawResponse);
            responseBody.close();
        }
    }
}
