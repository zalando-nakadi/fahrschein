package org.zalando.fahrschein.http.apache;

import com.github.luben.zstd.ZstdInputStream;
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
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.fahrschein.http.api.ContentType;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.fahrschein.http.api.Response;
import org.zalando.fahrschein.test.AbstractRequestFactoryTest;

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
public class HttpComponentsRequestFactoryTest extends AbstractRequestFactoryTest {

    public RequestFactory defaultRequestFactory(ContentEncoding contentEncoding) {
        return new HttpComponentsRequestFactory(HttpClients.createDefault(), contentEncoding);
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
        RequestFactory f = new HttpComponentsRequestFactory(httpClient, ContentEncoding.GZIP);
        Request r = f.createRequest(serverAddress.resolve("/timeout"), "GET");
        r.execute();
    }

}
