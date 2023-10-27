package org.zalando.fahrschein.http.api;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface Headers {

    String AUTHORIZATION = "Authorization";
    String CONTENT_LENGTH = "Content-Length";

    String TRANSFER_ENCODING = "Transfer-Encoding";

    String CONTENT_TYPE = "Content-Type";
    String CONTENT_ENCODING = "Content-Encoding";
    String ACCEPT_ENCODING = "Accept-Encoding";
    String USER_AGENT = "User-Agent";

    List<String> get(String headerName);

    void add(String headerName, String value);

    void put(String headerName, String value);

    @Nullable
    String getFirst(String headerName);

    Set<String> headerNames();

    long getContentLength();
    void setContentLength(long contentLength);

    ContentType getContentType();
    void setContentType(ContentType contentType);
}
