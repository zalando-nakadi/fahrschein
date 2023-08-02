package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Validated
public abstract class AbstractConfig {

    private String id;

    private Boolean autostartEnabled;

    private String nakadiUrl;

    private String applicationName;

    private String consumerGroup;

    private Position readFrom;

    private Boolean recordMetrics;

    private String objectMapperRef;

    @NestedConfigurationProperty
    protected OAuthConfig oauth = OAuthConfig.defaultOAuthConfig();

    @NestedConfigurationProperty
    protected HttpConfig http = new HttpConfig();

    @NestedConfigurationProperty
    protected AuthorizationsConfig authorizations = new AuthorizationsConfig();

    @NestedConfigurationProperty
    protected BackoffConfig backoff = BackoffConfig.defaultBackoffConfig();

    protected ThreadConfig threads = new ThreadConfig();

    public AbstractConfig() {
    }

    public String getId() {
        return this.id;
    }

    public Boolean getAutostartEnabled() {
        return this.autostartEnabled;
    }

    public String getNakadiUrl() {
        return this.nakadiUrl;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public String getConsumerGroup() {
        return this.consumerGroup;
    }

    public Position getReadFrom() {
        return this.readFrom;
    }

    public Boolean getRecordMetrics() {
        return this.recordMetrics;
    }

    public String getObjectMapperRef() {
        return this.objectMapperRef;
    }

    public OAuthConfig getOauth() {
        return this.oauth;
    }

    public HttpConfig getHttp() {
        return this.http;
    }

    public AuthorizationsConfig getAuthorizations() {
        return this.authorizations;
    }

    public BackoffConfig getBackoff() {
        return this.backoff;
    }

    public ThreadConfig getThreads() {
        return this.threads;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAutostartEnabled(Boolean autostartEnabled) {
        this.autostartEnabled = autostartEnabled;
    }

    public void setNakadiUrl(String nakadiUrl) {
        this.nakadiUrl = nakadiUrl;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public void setReadFrom(Position readFrom) {
        this.readFrom = readFrom;
    }

    public void setRecordMetrics(Boolean recordMetrics) {
        this.recordMetrics = recordMetrics;
    }

    public void setObjectMapperRef(String objectMapperRef) {
        this.objectMapperRef = objectMapperRef;
    }

    public void setOauth(OAuthConfig oauth) {
        this.oauth = oauth;
    }

    public void setHttp(HttpConfig http) {
        this.http = http;
    }

    public void setAuthorizations(AuthorizationsConfig authorizations) {
        this.authorizations = authorizations;
    }

    public void setBackoff(BackoffConfig backoff) {
        this.backoff = backoff;
    }

    public void setThreads(ThreadConfig threads) {
        this.threads = threads;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AbstractConfig)) return false;
        final AbstractConfig other = (AbstractConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$autostartEnabled = this.getAutostartEnabled();
        final Object other$autostartEnabled = other.getAutostartEnabled();
        if (this$autostartEnabled == null ? other$autostartEnabled != null : !this$autostartEnabled.equals(other$autostartEnabled))
            return false;
        final Object this$nakadiUrl = this.getNakadiUrl();
        final Object other$nakadiUrl = other.getNakadiUrl();
        if (this$nakadiUrl == null ? other$nakadiUrl != null : !this$nakadiUrl.equals(other$nakadiUrl)) return false;
        final Object this$applicationName = this.getApplicationName();
        final Object other$applicationName = other.getApplicationName();
        if (this$applicationName == null ? other$applicationName != null : !this$applicationName.equals(other$applicationName))
            return false;
        final Object this$consumerGroup = this.getConsumerGroup();
        final Object other$consumerGroup = other.getConsumerGroup();
        if (this$consumerGroup == null ? other$consumerGroup != null : !this$consumerGroup.equals(other$consumerGroup))
            return false;
        final Object this$readFrom = this.getReadFrom();
        final Object other$readFrom = other.getReadFrom();
        if (this$readFrom == null ? other$readFrom != null : !this$readFrom.equals(other$readFrom)) return false;
        final Object this$recordMetrics = this.getRecordMetrics();
        final Object other$recordMetrics = other.getRecordMetrics();
        if (this$recordMetrics == null ? other$recordMetrics != null : !this$recordMetrics.equals(other$recordMetrics))
            return false;
        final Object this$objectMapperRef = this.getObjectMapperRef();
        final Object other$objectMapperRef = other.getObjectMapperRef();
        if (this$objectMapperRef == null ? other$objectMapperRef != null : !this$objectMapperRef.equals(other$objectMapperRef))
            return false;
        final Object this$oauth = this.getOauth();
        final Object other$oauth = other.getOauth();
        if (this$oauth == null ? other$oauth != null : !this$oauth.equals(other$oauth)) return false;
        final Object this$http = this.getHttp();
        final Object other$http = other.getHttp();
        if (this$http == null ? other$http != null : !this$http.equals(other$http)) return false;
        final Object this$authorizations = this.getAuthorizations();
        final Object other$authorizations = other.getAuthorizations();
        if (this$authorizations == null ? other$authorizations != null : !this$authorizations.equals(other$authorizations))
            return false;
        final Object this$backoff = this.getBackoff();
        final Object other$backoff = other.getBackoff();
        if (this$backoff == null ? other$backoff != null : !this$backoff.equals(other$backoff)) return false;
        final Object this$threads = this.getThreads();
        final Object other$threads = other.getThreads();
        if (this$threads == null ? other$threads != null : !this$threads.equals(other$threads)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AbstractConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $autostartEnabled = this.getAutostartEnabled();
        result = result * PRIME + ($autostartEnabled == null ? 43 : $autostartEnabled.hashCode());
        final Object $nakadiUrl = this.getNakadiUrl();
        result = result * PRIME + ($nakadiUrl == null ? 43 : $nakadiUrl.hashCode());
        final Object $applicationName = this.getApplicationName();
        result = result * PRIME + ($applicationName == null ? 43 : $applicationName.hashCode());
        final Object $consumerGroup = this.getConsumerGroup();
        result = result * PRIME + ($consumerGroup == null ? 43 : $consumerGroup.hashCode());
        final Object $readFrom = this.getReadFrom();
        result = result * PRIME + ($readFrom == null ? 43 : $readFrom.hashCode());
        final Object $recordMetrics = this.getRecordMetrics();
        result = result * PRIME + ($recordMetrics == null ? 43 : $recordMetrics.hashCode());
        final Object $objectMapperRef = this.getObjectMapperRef();
        result = result * PRIME + ($objectMapperRef == null ? 43 : $objectMapperRef.hashCode());
        final Object $oauth = this.getOauth();
        result = result * PRIME + ($oauth == null ? 43 : $oauth.hashCode());
        final Object $http = this.getHttp();
        result = result * PRIME + ($http == null ? 43 : $http.hashCode());
        final Object $authorizations = this.getAuthorizations();
        result = result * PRIME + ($authorizations == null ? 43 : $authorizations.hashCode());
        final Object $backoff = this.getBackoff();
        result = result * PRIME + ($backoff == null ? 43 : $backoff.hashCode());
        final Object $threads = this.getThreads();
        result = result * PRIME + ($threads == null ? 43 : $threads.hashCode());
        return result;
    }

    public String toString() {
        return "AbstractConfig(id=" + this.getId() + ", autostartEnabled=" + this.getAutostartEnabled() + ", nakadiUrl=" + this.getNakadiUrl() + ", applicationName=" + this.getApplicationName() + ", consumerGroup=" + this.getConsumerGroup() + ", readFrom=" + this.getReadFrom() + ", recordMetrics=" + this.getRecordMetrics() + ", objectMapperRef=" + this.getObjectMapperRef() + ", oauth=" + this.getOauth() + ", http=" + this.getHttp() + ", authorizations=" + this.getAuthorizations() + ", backoff=" + this.getBackoff() + ", threads=" + this.getThreads() + ")";
    }
}
