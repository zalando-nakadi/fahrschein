package org.zalando.fahrschein.http.apache;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HTTP;
import org.zalando.fahrschein.http.api.Headers;
import org.zalando.fahrschein.http.api.HeadersImpl;
import org.zalando.fahrschein.http.api.Request;
import org.zalando.fahrschein.http.api.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

/**
 * {@link Request} implementation based on Apache HttpComponents HttpClient.
 *
 * <p>Created via the {@link HttpComponentsRequestFactory}.
 *
 * @author Oleg Kalnichevski
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Joern Horstmann
 * @see HttpComponentsRequestFactory#createRequest(URI, String)
 */
final class HttpComponentsRequest implements Request {

	private final HttpClient httpClient;
	private final HttpUriRequest httpRequest;

	private final Headers headers;
	private ByteArrayOutputStream bufferedOutput;
	private boolean executed;

	HttpComponentsRequest(HttpClient client, HttpUriRequest request) {
		this.httpClient = client;
		this.httpRequest = request;
		this.headers = new HeadersImpl();
	}

	@Override
	public String getMethod() {
		return this.httpRequest.getMethod();
	}

	@Override
	public URI getURI() {
		return this.httpRequest.getURI();
	}

	private Response executeInternal(Headers headers) throws IOException {
		final byte[] bytes = this.bufferedOutput != null ? this.bufferedOutput.toByteArray() : new byte[0];

		if (headers.getContentLength() < 0) {
			headers.setContentLength(bytes.length);
		}

		for (String headerName : headers.headerNames()) {
			final List<String> value = headers.get(headerName);
			if (!HTTP.CONTENT_LEN.equalsIgnoreCase(headerName) && !HTTP.TRANSFER_ENCODING.equalsIgnoreCase(headerName)) {
				for (String headerValue : value) {
					this.httpRequest.addHeader(headerName, headerValue);
				}
			}
		}

		if (this.httpRequest instanceof HttpEntityEnclosingRequest) {
			HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest) this.httpRequest;
			HttpEntity requestEntity = new ByteArrayEntity(bytes);
			entityEnclosingRequest.setEntity(requestEntity);
		}

		final HttpResponse httpResponse = this.httpClient.execute(this.httpRequest);
		final Response result = new HttpComponentsResponse(httpResponse);
		this.bufferedOutput = null;

		return result;
	}

	@Override
	public final Headers getHeaders() {
		return (this.executed ? new HeadersImpl(this.headers, true) : this.headers);
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
	public final Response execute() throws IOException {
		assertNotExecuted();
		final Response result = executeInternal(this.headers);
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
