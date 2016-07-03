package org.zalando.fahrschein.request;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

final class HttpComponentsClientHttpRequest extends AbstractClientHttpRequest {

    private ByteArrayOutputStream bufferedBody = new ByteArrayOutputStream(1024);

    private final CloseableHttpClient httpClient;
    private final HttpUriRequest httpRequest;
    private final HttpContext httpContext;


    HttpComponentsClientHttpRequest(CloseableHttpClient httpClient, HttpUriRequest httpRequest, HttpContext httpContext) {
        this.httpClient = httpClient;
        this.httpRequest = httpRequest;
        this.httpContext = httpContext;
    }

    @Override
    protected OutputStream getBodyInternal(final HttpHeaders headers) throws IOException {
        return bufferedBody;
    }

    @Override
    protected ClientHttpResponse executeInternal(final HttpHeaders headers) throws IOException {
        final byte[] bytes = bufferedBody.toByteArray();
        if (headers.getContentLength() == -1) {
            headers.setContentLength(bytes.length);
        }
        addHeaders(headers);
        setEntity(bytes);

        return new HttpComponentsClientHttpResponse(httpClient.execute(httpRequest, httpContext));
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(httpRequest.getMethod());
    }

    @Override
    public URI getURI() {
        return httpRequest.getURI();
    }

    private void addHeaders(final HttpHeaders headers) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            if (HttpHeaders.COOKIE.equalsIgnoreCase(headerName)) {  // RFC 6265
                String headerValue = StringUtils.collectionToDelimitedString(entry.getValue(), "; ");
                httpRequest.addHeader(headerName, headerValue);
            } else if (!HTTP.CONTENT_LEN.equalsIgnoreCase(headerName) &&
                    !HTTP.TRANSFER_ENCODING.equalsIgnoreCase(headerName)) {
                for (String headerValue : entry.getValue()) {
                    httpRequest.addHeader(headerName, headerValue);
                }
            }
        }
    }

    private void setEntity(final byte[] bytes) {
        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) httpRequest;
            request.setEntity(new ByteArrayEntity(bytes));
        }
    }
}
