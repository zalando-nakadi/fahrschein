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

    private TimeSpan connectionRequestTimeout;

    private Boolean contentCompressionEnabled;

    private Integer bufferSize;

    private TimeSpan connectionTimeToLive;

    private Integer maxConnectionsTotal;

    private Integer maxConnectionsPerRoute;

    private Boolean evictExpiredConnections;

    private Boolean evictIdleConnections;

    private Long maxIdleTime;

    private String userAgent;

    private ContentEncoding contentEncoding;

    public static HttpConfig defaultHttpConfig() {
        HttpConfig config = new HttpConfig();
        config.setSocketTimeout(TimeSpan.of(60, TimeUnit.SECONDS));
        config.setConnectTimeout(TimeSpan.of(2000, TimeUnit.MILLISECONDS));
        config.setConnectionRequestTimeout(TimeSpan.of(8000, TimeUnit.MILLISECONDS));
        config.setContentCompressionEnabled(true);
        config.setBufferSize(512);
        config.setConnectionTimeToLive(TimeSpan.of(30, TimeUnit.SECONDS));
        config.setMaxConnectionsTotal(3);
        config.setMaxConnectionsPerRoute(3);
        config.setEvictExpiredConnections(true);
        config.setEvictIdleConnections(true);
        config.setMaxIdleTime(Long.valueOf(10_000));
        config.setUserAgent("fahrschein-spring-boot-starter");
        config.setContentEncoding(ContentEncoding.GZIP);
        return config;
    }

    public void mergeFromDefaults(HttpConfig http) {
        this.setSocketTimeout(Optional.ofNullable(this.getSocketTimeout()).orElse(http.getSocketTimeout()));
        this.setConnectTimeout(Optional.ofNullable(this.getConnectTimeout()).orElse(http.getConnectTimeout()));
        this.setConnectionRequestTimeout(Optional.ofNullable(this.getConnectionRequestTimeout()).orElse(http.getConnectionRequestTimeout()));
        this.setContentCompressionEnabled(Optional.ofNullable(this.getContentCompressionEnabled()).orElse(http.getContentCompressionEnabled()));
        this.setBufferSize(Optional.ofNullable(this.getBufferSize()).orElse(http.getBufferSize()));
        this.setConnectionTimeToLive(Optional.ofNullable(this.getConnectionTimeToLive()).orElse(http.getConnectionTimeToLive()));
        this.setMaxConnectionsTotal(Optional.ofNullable(this.getMaxConnectionsTotal()).orElse(http.getMaxConnectionsTotal()));
        this.setMaxConnectionsPerRoute(Optional.ofNullable(this.getMaxConnectionsPerRoute()).orElse(http.getMaxConnectionsPerRoute()));
        this.setEvictExpiredConnections(Optional.ofNullable(this.getEvictExpiredConnections()).orElse(http.getEvictExpiredConnections()));
        this.setEvictIdleConnections(Optional.ofNullable(this.getEvictIdleConnections()).orElse(http.getEvictIdleConnections()));
        this.setMaxIdleTime(Optional.ofNullable(this.getMaxIdleTime()).orElse(http.getMaxIdleTime()));
        this.setUserAgent(Optional.ofNullable(this.getUserAgent()).orElse(http.getUserAgent()));
        this.setContentEncoding(Optional.ofNullable(this.getContentEncoding()).orElse(http.getContentEncoding()));
    }

}
