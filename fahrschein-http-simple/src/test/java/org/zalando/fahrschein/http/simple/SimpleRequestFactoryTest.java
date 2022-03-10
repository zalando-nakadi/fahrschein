package org.zalando.fahrschein.http.simple;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.Response;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SimpleRequestFactoryTest {

    static HttpServer server;
    static URI serverAddress;

    @BeforeClass
    public static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 1);
        serverAddress = URI.create("http://localhost:" + server.getAddress().getPort());
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    @Captor
    public ArgumentCaptor<HttpExchange> exchangeCaptor;

    @Test
    public void testGetRequest() throws IOException {
        // given
        String expectedResponse = "{}";
        HttpHandler spy = Mockito.spy(new SimpleResponseContentHandler(expectedResponse));
        server.createContext("/get", spy);

        // when
        SimpleRequestFactory f = new SimpleRequestFactory();
        Request r = f.createRequest(serverAddress.resolve("/get"), "GET");
        Response executed = r.execute();
        String actualResponse = readResponse(executed);

        // then
        assertEquals(serverAddress.resolve("/get"), r.getURI());
        Mockito.verify(spy).handle(exchangeCaptor.capture());
        HttpExchange capturedArgument = exchangeCaptor.<HttpExchange> getValue();
        assertEquals("GET", capturedArgument.getRequestMethod());
        assertEquals(200, executed.getStatusCode());
        assertEquals("OK", executed.getStatusText());
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testPostRequest() throws IOException {
        // given
        String expectedResponse = "{}";
        HttpHandler spy = Mockito.spy(new SimpleResponseContentHandler(expectedResponse));
        server.createContext("/post", spy);

        // when
        SimpleRequestFactory f = new SimpleRequestFactory();
        Request r = f.createRequest(serverAddress.resolve("/post"), "POST");
        r.getHeaders().setContentType(ContentType.APPLICATION_JSON);
        try (final OutputStream body = r.getBody()) {
            body.write("{}".getBytes());
        }
        Response executed = r.execute();
        String actualResponse = readResponse(executed);

        // then
        Mockito.verify(spy).handle(exchangeCaptor.capture());
        HttpExchange capturedArgument = exchangeCaptor.getValue();
        assertEquals("POST", capturedArgument.getRequestMethod());
        assertEquals(URI.create("/post"), capturedArgument.getRequestURI());
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testGzippedResponse() throws IOException {
        // given
        String expectedResponse = "{}";
        HttpHandler spy = Mockito.spy(new GzippedResponseContentHandler(expectedResponse));
        server.createContext("/gzipped", spy);

        // when
        SimpleRequestFactory f = new SimpleRequestFactory();
        Request r = f.createRequest(serverAddress.resolve("/gzipped"), "GET");
        Response executed = r.execute();
        String actualResponse = readResponse(executed);

        // then
        Mockito.verify(spy).handle(exchangeCaptor.capture());
        HttpExchange capturedArgument = exchangeCaptor.getValue();
        assertThat("accept-encoding header", capturedArgument.getRequestHeaders().get("accept-encoding"), equalTo(List.of("gzip")));
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
        SimpleRequestFactory f = new SimpleRequestFactory();
        f.setReadTimeout(1);
        Request r = f.createRequest(serverAddress.resolve("/timeout"), "GET");
        r.execute();
    }

    private String readResponse(Response executed) throws IOException {
        String res = new BufferedReader(
                new InputStreamReader(executed.getBody(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        executed.close();
        return res;
    }

    private static class SimpleResponseContentHandler implements HttpHandler {

        private final byte[] rawResponse;

        SimpleResponseContentHandler(String response) {
            this.rawResponse = response.getBytes(StandardCharsets.UTF_8);
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(200, rawResponse.length);
            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write(rawResponse);
            responseBody.close();
        }
    }

    private static class GzippedResponseContentHandler implements HttpHandler {

        private final byte[] rawResponse;

        GzippedResponseContentHandler(String response) throws IOException {
            byte[] stringResponse = response.getBytes(StandardCharsets.UTF_8);
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
            responseBody.flush();
            responseBody.close();
        }
    }
}
