/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.fahrschein.http.apache;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link ClientHttpResponse} implementation based on
 * Apache HttpComponents HttpClient.
 *
 * <p>Created via the {@link HttpComponentsClientHttpRequest}.
 *
 * @author Oleg Kalnichevski
 * @author Arjen Poutsma
 * @author Joern Horstmann
 * @see HttpComponentsClientHttpRequest#execute()
 */
final class HttpComponentsClientHttpResponse implements ClientHttpResponse {

    private final HttpResponse httpResponse;
    private HttpHeaders headers;

    HttpComponentsClientHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return this.httpResponse.getStatusLine().getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return this.httpResponse.getStatusLine().getReasonPhrase();
    }

    @Override
    public HttpHeaders getHeaders() {
        if (this.headers == null) {
            this.headers = new HttpHeaders();
            for (Header header : this.httpResponse.getAllHeaders()) {
                this.headers.add(header.getName(), header.getValue());
            }
        }
        return this.headers;
    }

    @Override
    public InputStream getBody() throws IOException {
        HttpEntity entity = this.httpResponse.getEntity();
        return (entity != null ? entity.getContent() : new ByteArrayInputStream(new byte[0]));
    }

    @Override
    public void close() {
        // Release underlying connection back to the connection manager
        if (this.httpResponse instanceof Closeable) {
            try {
                ((Closeable) this.httpResponse).close();
            } catch (IOException e) {
                // ignore exception on close
            }
        }
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.valueOf(getRawStatusCode());
    }
}
