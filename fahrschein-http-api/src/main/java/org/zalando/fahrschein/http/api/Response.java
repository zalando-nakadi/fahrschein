package org.zalando.fahrschein.http.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface Response extends Closeable {

	int getStatusCode() throws IOException;

	String getStatusText() throws IOException;

	Headers getHeaders();

	InputStream getBody() throws IOException;

	@Override
	void close();

}
