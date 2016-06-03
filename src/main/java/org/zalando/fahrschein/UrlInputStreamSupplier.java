package org.zalando.fahrschein;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class UrlInputStreamSupplier implements InputStreamSupplier {

    private final URL endpoint;

    public UrlInputStreamSupplier(URL endpoint) {
        this.endpoint = endpoint;
    }

    public InputStream open(ConnectionParameters connectionParameters) throws IOException {
        final HttpURLConnection urlConnection = (HttpURLConnection) endpoint.openConnection();

        urlConnection.setConnectTimeout(connectionParameters.getConnectTimeout());
        urlConnection.setReadTimeout(connectionParameters.getReadTimeout());
        //urlConnection.setConnectTimeout((int)(Math.random() * 100));
        //urlConnection.setReadTimeout(15000 + (int)(Math.random() * 30000));
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
            return urlConnection.getErrorStream();
        }
    }
}
