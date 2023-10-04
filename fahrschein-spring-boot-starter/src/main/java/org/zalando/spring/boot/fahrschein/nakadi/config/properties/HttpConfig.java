package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.Data;
import lombok.ToString;
import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.spring.boot.fahrschein.config.TimeSpan;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Data
@ToString
public class HttpConfig {

    private TimeSpan socketTimeout;

    private TimeSpan connectTimeout;

    private TimeSpan requestTimeout;

    private ContentEncoding contentEncoding;

    public static HttpConfig defaultHttpConfig() {
        HttpConfig config = new HttpConfig();
        config.setSocketTimeout(TimeSpan.of(60, TimeUnit.SECONDS));
        config.setRequestTimeout(TimeSpan.of(60, TimeUnit.SECONDS));
        config.setConnectTimeout(TimeSpan.of(2000, TimeUnit.MILLISECONDS));
        config.setContentEncoding(ContentEncoding.GZIP);
        return config;
    }

    public void mergeFromDefaults(HttpConfig http) {
        this.setSocketTimeout(Optional.ofNullable(this.getSocketTimeout()).orElse(http.getSocketTimeout()));
        this.setRequestTimeout(Optional.ofNullable(this.getRequestTimeout()).orElse(http.getRequestTimeout()));
        this.setConnectTimeout(Optional.ofNullable(this.getConnectTimeout()).orElse(http.getConnectTimeout()));
        this.setContentEncoding(Optional.ofNullable(this.getContentEncoding()).orElse(http.getContentEncoding()));
    }

}
