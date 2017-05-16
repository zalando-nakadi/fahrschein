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

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link ClientHttpRequest} implementation based on
 * Apache HttpComponents HttpClient.
 *
 * <p>Created via the {@link HttpComponentsClientHttpRequestFactory}.
 *
 * @author Oleg Kalnichevski
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Joern Horstmann
 * @see HttpComponentsClientHttpRequestFactory#createRequest(URI, HttpMethod)
 */
final class HttpComponentsClientHttpRequest implements ClientHttpRequest {

	private final HttpClient httpClient;
	private final HttpUriRequest httpRequest;

	private final HttpContext httpContext;
	private final HttpHeaders headers;
	private ByteArrayOutputStream bufferedOutput;
	private boolean executed;


	HttpComponentsClientHttpRequest(HttpClient client, HttpUriRequest request, HttpContext context) {
		this.httpClient = client;
		this.httpRequest = request;
		this.httpContext = context;
		this.headers = new HttpHeaders();
	}


	@Override
	public HttpMethod getMethod() {
		return Enum.valueOf(HttpMethod.class, this.httpRequest.getMethod());
	}

	@Override
	public URI getURI() {
		return this.httpRequest.getURI();
	}

	private static String collectionToDelimitedString(Collection<?> coll, String delim) {
		if (coll == null || coll.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Iterator<?> it = coll.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}

	private ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
		final byte[] bytes = this.bufferedOutput != null ? this.bufferedOutput.toByteArray() : new byte[0];

		if (headers.getContentLength() < 0) {
			headers.setContentLength(bytes.length);
		}

		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			String headerName = entry.getKey();
			if (HttpHeaders.COOKIE.equalsIgnoreCase(headerName)) {  // RFC 6265
				String headerValue = collectionToDelimitedString(entry.getValue(), "; ");
				this.httpRequest.addHeader(headerName, headerValue);
			}
			else if (!HTTP.CONTENT_LEN.equalsIgnoreCase(headerName) &&
					!HTTP.TRANSFER_ENCODING.equalsIgnoreCase(headerName)) {
				for (String headerValue : entry.getValue()) {
					this.httpRequest.addHeader(headerName, headerValue);
				}
			}
		}

		if (this.httpRequest instanceof HttpEntityEnclosingRequest) {
			HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest) this.httpRequest;
			HttpEntity requestEntity = new ByteArrayEntity(bytes);
			entityEnclosingRequest.setEntity(requestEntity);
		}

		final HttpResponse httpResponse = this.httpClient.execute(this.httpRequest, this.httpContext);
		final ClientHttpResponse result = new HttpComponentsClientHttpResponse(httpResponse);
		this.bufferedOutput = null;
		return result;
	}

	@Override
	public final HttpHeaders getHeaders() {
		return (this.executed ? HttpHeaders.readOnlyHttpHeaders(this.headers) : this.headers);
	}

	@Override
	public final OutputStream getBody() throws IOException {
		assertNotExecuted();
		if (this.bufferedOutput == null) {
			this.bufferedOutput =  new ByteArrayOutputStream(1024);
		}
		return this.bufferedOutput;
	}

	@Override
	public final ClientHttpResponse execute() throws IOException {
		assertNotExecuted();
		final ClientHttpResponse result = executeInternal(this.headers);
		this.executed = true;
		return result;
	}

	/**
	 * Assert that this request has not been {@linkplain #execute() executed} yet.
	 * @throws IllegalStateException if this request has been executed
	 */
	private void assertNotExecuted() {
		if (this.executed) {
			throw new IllegalStateException("ClientHttpRequest already executed");
		}
	}
}
