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

package org.zalando.fahrschein.http.simple;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link ClientHttpRequest} implementation that uses standard JDK facilities to
 * execute buffered requests. Created via the {@link SimpleClientHttpRequestFactory}.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Joern Horstmann
 * @see SimpleClientHttpRequestFactory#createRequest(java.net.URI, HttpMethod)
 */
final class SimpleBufferingClientHttpRequest implements ClientHttpRequest {

	private final HttpURLConnection connection;
	private final HttpHeaders headers;
	private ByteArrayOutputStream bufferedOutput;
	private boolean executed;

	SimpleBufferingClientHttpRequest(HttpURLConnection connection) {
		this.connection = connection;
		this.headers = new HttpHeaders();
	}

	@Override
	public HttpMethod getMethod() {
		return Enum.valueOf(HttpMethod.class, this.connection.getRequestMethod());
	}

	@Override
	public URI getURI() {
		try {
			return this.connection.getURL().toURI();
		} catch (URISyntaxException ex) {
			throw new IllegalStateException("Could not get HttpURLConnection URI: " + ex.getMessage(), ex);
		}
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

	private ClientHttpResponse executeInternal() throws IOException {
		final int size = this.bufferedOutput != null ? this.bufferedOutput.size() : 0;
		if (this.headers.getContentLength() < 0) {
			this.headers.setContentLength(size);
		}

		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			final String headerName = entry.getKey();
			if (HttpHeaders.COOKIE.equalsIgnoreCase(headerName)) {  // RFC 6265
				final String headerValue = collectionToDelimitedString(entry.getValue(), "; ");
				connection.setRequestProperty(headerName, headerValue);
			} else {
				for (String headerValue : entry.getValue()) {
					final String actualHeaderValue = headerValue != null ? headerValue : "";
					connection.addRequestProperty(headerName, actualHeaderValue);
				}
			}
		}

		// JDK <1.8 doesn't support getOutputStream with HTTP DELETE
		if (HttpMethod.DELETE == getMethod() && size > 0) {
			this.connection.setDoOutput(false);
		}
		if (this.connection.getDoOutput()) {
			this.connection.setFixedLengthStreamingMode(size);
		}

		this.connection.connect();

		if (this.connection.getDoOutput() && this.bufferedOutput != null) {
			this.bufferedOutput.writeTo(this.connection.getOutputStream());
		} else {
			// Immediately trigger the request in a no-output scenario as well
			this.connection.getResponseCode();
		}

		final ClientHttpResponse result = new SimpleClientHttpResponse(this.connection);
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
			this.bufferedOutput = new ByteArrayOutputStream(1024);
		}
		return this.bufferedOutput;
	}

	@Override
	public final ClientHttpResponse execute() throws IOException {
		assertNotExecuted();
		final ClientHttpResponse result = executeInternal();
		this.executed = true;
		return result;
	}

	/**
	 * Assert that this request has not been {@linkplain #execute() executed} yet.
	 * @throws IllegalStateException if this request has been executed
	 */
	protected void assertNotExecuted() {
		if (this.executed) {
			throw new IllegalStateException("ClientHttpRequest already executed");
		}
	}
}
