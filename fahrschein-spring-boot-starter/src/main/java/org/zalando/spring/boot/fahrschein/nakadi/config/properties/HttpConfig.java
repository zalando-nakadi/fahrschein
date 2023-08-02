package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.zalando.fahrschein.http.api.ContentEncoding;
import org.zalando.spring.boot.fahrschein.config.TimeSpan;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    public HttpConfig() {
    }

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

    public TimeSpan getSocketTimeout() {
        return this.socketTimeout;
    }

    public TimeSpan getConnectTimeout() {
        return this.connectTimeout;
    }

    public TimeSpan getConnectionRequestTimeout() {
        return this.connectionRequestTimeout;
    }

    public Boolean getContentCompressionEnabled() {
        return this.contentCompressionEnabled;
    }

    public Integer getBufferSize() {
        return this.bufferSize;
    }

    public TimeSpan getConnectionTimeToLive() {
        return this.connectionTimeToLive;
    }

    public Integer getMaxConnectionsTotal() {
        return this.maxConnectionsTotal;
    }

    public Integer getMaxConnectionsPerRoute() {
        return this.maxConnectionsPerRoute;
    }

    public Boolean getEvictExpiredConnections() {
        return this.evictExpiredConnections;
    }

    public Boolean getEvictIdleConnections() {
        return this.evictIdleConnections;
    }

    public Long getMaxIdleTime() {
        return this.maxIdleTime;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public ContentEncoding getContentEncoding() {
        return this.contentEncoding;
    }

    public void setSocketTimeout(TimeSpan socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public void setConnectTimeout(TimeSpan connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setConnectionRequestTimeout(TimeSpan connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public void setContentCompressionEnabled(Boolean contentCompressionEnabled) {
        this.contentCompressionEnabled = contentCompressionEnabled;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setConnectionTimeToLive(TimeSpan connectionTimeToLive) {
        this.connectionTimeToLive = connectionTimeToLive;
    }

    public void setMaxConnectionsTotal(Integer maxConnectionsTotal) {
        this.maxConnectionsTotal = maxConnectionsTotal;
    }

    public void setMaxConnectionsPerRoute(Integer maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public void setEvictExpiredConnections(Boolean evictExpiredConnections) {
        this.evictExpiredConnections = evictExpiredConnections;
    }

    public void setEvictIdleConnections(Boolean evictIdleConnections) {
        this.evictIdleConnections = evictIdleConnections;
    }

    public void setMaxIdleTime(Long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setContentEncoding(ContentEncoding contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof HttpConfig)) return false;
        final HttpConfig other = (HttpConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$socketTimeout = this.getSocketTimeout();
        final Object other$socketTimeout = other.getSocketTimeout();
        if (this$socketTimeout == null ? other$socketTimeout != null : !this$socketTimeout.equals(other$socketTimeout))
            return false;
        final Object this$connectTimeout = this.getConnectTimeout();
        final Object other$connectTimeout = other.getConnectTimeout();
        if (this$connectTimeout == null ? other$connectTimeout != null : !this$connectTimeout.equals(other$connectTimeout))
            return false;
        final Object this$connectionRequestTimeout = this.getConnectionRequestTimeout();
        final Object other$connectionRequestTimeout = other.getConnectionRequestTimeout();
        if (this$connectionRequestTimeout == null ? other$connectionRequestTimeout != null : !this$connectionRequestTimeout.equals(other$connectionRequestTimeout))
            return false;
        final Object this$contentCompressionEnabled = this.getContentCompressionEnabled();
        final Object other$contentCompressionEnabled = other.getContentCompressionEnabled();
        if (this$contentCompressionEnabled == null ? other$contentCompressionEnabled != null : !this$contentCompressionEnabled.equals(other$contentCompressionEnabled))
            return false;
        final Object this$bufferSize = this.getBufferSize();
        final Object other$bufferSize = other.getBufferSize();
        if (this$bufferSize == null ? other$bufferSize != null : !this$bufferSize.equals(other$bufferSize))
            return false;
        final Object this$connectionTimeToLive = this.getConnectionTimeToLive();
        final Object other$connectionTimeToLive = other.getConnectionTimeToLive();
        if (this$connectionTimeToLive == null ? other$connectionTimeToLive != null : !this$connectionTimeToLive.equals(other$connectionTimeToLive))
            return false;
        final Object this$maxConnectionsTotal = this.getMaxConnectionsTotal();
        final Object other$maxConnectionsTotal = other.getMaxConnectionsTotal();
        if (this$maxConnectionsTotal == null ? other$maxConnectionsTotal != null : !this$maxConnectionsTotal.equals(other$maxConnectionsTotal))
            return false;
        final Object this$maxConnectionsPerRoute = this.getMaxConnectionsPerRoute();
        final Object other$maxConnectionsPerRoute = other.getMaxConnectionsPerRoute();
        if (this$maxConnectionsPerRoute == null ? other$maxConnectionsPerRoute != null : !this$maxConnectionsPerRoute.equals(other$maxConnectionsPerRoute))
            return false;
        final Object this$evictExpiredConnections = this.getEvictExpiredConnections();
        final Object other$evictExpiredConnections = other.getEvictExpiredConnections();
        if (this$evictExpiredConnections == null ? other$evictExpiredConnections != null : !this$evictExpiredConnections.equals(other$evictExpiredConnections))
            return false;
        final Object this$evictIdleConnections = this.getEvictIdleConnections();
        final Object other$evictIdleConnections = other.getEvictIdleConnections();
        if (this$evictIdleConnections == null ? other$evictIdleConnections != null : !this$evictIdleConnections.equals(other$evictIdleConnections))
            return false;
        final Object this$maxIdleTime = this.getMaxIdleTime();
        final Object other$maxIdleTime = other.getMaxIdleTime();
        if (this$maxIdleTime == null ? other$maxIdleTime != null : !this$maxIdleTime.equals(other$maxIdleTime))
            return false;
        final Object this$userAgent = this.getUserAgent();
        final Object other$userAgent = other.getUserAgent();
        if (this$userAgent == null ? other$userAgent != null : !this$userAgent.equals(other$userAgent)) return false;
        final Object this$contentEncoding = this.getContentEncoding();
        final Object other$contentEncoding = other.getContentEncoding();
        if (this$contentEncoding == null ? other$contentEncoding != null : !this$contentEncoding.equals(other$contentEncoding))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof HttpConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $socketTimeout = this.getSocketTimeout();
        result = result * PRIME + ($socketTimeout == null ? 43 : $socketTimeout.hashCode());
        final Object $connectTimeout = this.getConnectTimeout();
        result = result * PRIME + ($connectTimeout == null ? 43 : $connectTimeout.hashCode());
        final Object $connectionRequestTimeout = this.getConnectionRequestTimeout();
        result = result * PRIME + ($connectionRequestTimeout == null ? 43 : $connectionRequestTimeout.hashCode());
        final Object $contentCompressionEnabled = this.getContentCompressionEnabled();
        result = result * PRIME + ($contentCompressionEnabled == null ? 43 : $contentCompressionEnabled.hashCode());
        final Object $bufferSize = this.getBufferSize();
        result = result * PRIME + ($bufferSize == null ? 43 : $bufferSize.hashCode());
        final Object $connectionTimeToLive = this.getConnectionTimeToLive();
        result = result * PRIME + ($connectionTimeToLive == null ? 43 : $connectionTimeToLive.hashCode());
        final Object $maxConnectionsTotal = this.getMaxConnectionsTotal();
        result = result * PRIME + ($maxConnectionsTotal == null ? 43 : $maxConnectionsTotal.hashCode());
        final Object $maxConnectionsPerRoute = this.getMaxConnectionsPerRoute();
        result = result * PRIME + ($maxConnectionsPerRoute == null ? 43 : $maxConnectionsPerRoute.hashCode());
        final Object $evictExpiredConnections = this.getEvictExpiredConnections();
        result = result * PRIME + ($evictExpiredConnections == null ? 43 : $evictExpiredConnections.hashCode());
        final Object $evictIdleConnections = this.getEvictIdleConnections();
        result = result * PRIME + ($evictIdleConnections == null ? 43 : $evictIdleConnections.hashCode());
        final Object $maxIdleTime = this.getMaxIdleTime();
        result = result * PRIME + ($maxIdleTime == null ? 43 : $maxIdleTime.hashCode());
        final Object $userAgent = this.getUserAgent();
        result = result * PRIME + ($userAgent == null ? 43 : $userAgent.hashCode());
        final Object $contentEncoding = this.getContentEncoding();
        result = result * PRIME + ($contentEncoding == null ? 43 : $contentEncoding.hashCode());
        return result;
    }

    public String toString() {
        return "HttpConfig(socketTimeout=" + this.getSocketTimeout() + ", connectTimeout=" + this.getConnectTimeout() + ", connectionRequestTimeout=" + this.getConnectionRequestTimeout() + ", contentCompressionEnabled=" + this.getContentCompressionEnabled() + ", bufferSize=" + this.getBufferSize() + ", connectionTimeToLive=" + this.getConnectionTimeToLive() + ", maxConnectionsTotal=" + this.getMaxConnectionsTotal() + ", maxConnectionsPerRoute=" + this.getMaxConnectionsPerRoute() + ", evictExpiredConnections=" + this.getEvictExpiredConnections() + ", evictIdleConnections=" + this.getEvictIdleConnections() + ", maxIdleTime=" + this.getMaxIdleTime() + ", userAgent=" + this.getUserAgent() + ", contentEncoding=" + this.getContentEncoding() + ")";
    }
}
