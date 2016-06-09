package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.zalando.problem.ProblemModule;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class UrlInputStreamSupplier implements InputStreamSupplier {

    private final URL endpoint;
    private ObjectMapper objectMapper;

    public UrlInputStreamSupplier(final URL endpoint) {
        this.endpoint = endpoint;
        this.objectMapper = createProblemObjectMapper();
    }

    private ObjectMapper createProblemObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.registerModule(new GuavaModule());
        objectMapper.registerModule(new ProblemModule());
        return objectMapper;
    }

    public InputStream open(ConnectionParameters connectionParameters) throws IOException {
        final HttpURLConnection urlConnection = (HttpURLConnection) endpoint.openConnection();

        urlConnection.setConnectTimeout(connectionParameters.getConnectTimeout());
        urlConnection.setReadTimeout(connectionParameters.getReadTimeout());
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(false);
        urlConnection.setInstanceFollowRedirects(true);

        final Map<String, String> headers = connectionParameters.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        urlConnection.connect();

        final int responseCode = urlConnection.getResponseCode();
        if (responseCode >= 200 && responseCode <= 299) {
            return urlConnection.getInputStream();
        } else {
            final IOProblem problem = objectMapper.readValue(urlConnection.getErrorStream(), IOProblem.class);
            throw problem;
        }
    }
}
