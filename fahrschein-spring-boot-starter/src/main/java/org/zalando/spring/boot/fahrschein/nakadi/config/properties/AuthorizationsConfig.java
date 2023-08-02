package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuthorizationsConfig {

    @NestedConfigurationProperty
    private AuthorizationUserServiceTeamLists admins = new AuthorizationUserServiceTeamLists();

    @NestedConfigurationProperty
    private AuthorizationUserServiceTeamLists readers = new AuthorizationUserServiceTeamLists();

    @NestedConfigurationProperty
    private AuthorizationUserServiceTeamLists writers = new AuthorizationUserServiceTeamLists();

    private Boolean anyReader = Boolean.FALSE;

    public AuthorizationsConfig() {
    }

    public void mergeFromDefaults(AuthorizationsConfig defaultConfig) {

        this.setAdmins(merge(this.admins, defaultConfig.admins));
        this.setReaders(merge(this.readers, defaultConfig.readers));
        this.setWriters(merge(this.writers, defaultConfig.writers));

        if (defaultConfig.getAnyReader() || this.getAnyReader()) {
            this.setAnyReader(Boolean.TRUE);
        }
    }

    private AuthorizationUserServiceTeamLists merge(AuthorizationUserServiceTeamLists one, AuthorizationUserServiceTeamLists other) {
        AuthorizationUserServiceTeamLists result = new AuthorizationUserServiceTeamLists();
        result.getUsers().addAll(mergeLists(one.getUsers(), other.getUsers()));
        result.getServices().addAll(mergeLists(one.getServices(), other.getServices()));
        result.getTeams().addAll(mergeLists(one.getTeams(), other.getTeams()));
        return result;
    }

    private Collection<String> mergeLists(List<String> one, List<String> other) {
        Set<String> merged = new HashSet<>(one);
        merged.addAll(other);
        return merged;
    }

    public AuthorizationUserServiceTeamLists getAdmins() {
        return this.admins;
    }

    public AuthorizationUserServiceTeamLists getReaders() {
        return this.readers;
    }

    public AuthorizationUserServiceTeamLists getWriters() {
        return this.writers;
    }

    public Boolean getAnyReader() {
        return this.anyReader;
    }

    public void setAdmins(AuthorizationUserServiceTeamLists admins) {
        this.admins = admins;
    }

    public void setReaders(AuthorizationUserServiceTeamLists readers) {
        this.readers = readers;
    }

    public void setWriters(AuthorizationUserServiceTeamLists writers) {
        this.writers = writers;
    }

    public void setAnyReader(Boolean anyReader) {
        this.anyReader = anyReader;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AuthorizationsConfig)) return false;
        final AuthorizationsConfig other = (AuthorizationsConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$admins = this.getAdmins();
        final Object other$admins = other.getAdmins();
        if (this$admins == null ? other$admins != null : !this$admins.equals(other$admins)) return false;
        final Object this$readers = this.getReaders();
        final Object other$readers = other.getReaders();
        if (this$readers == null ? other$readers != null : !this$readers.equals(other$readers)) return false;
        final Object this$writers = this.getWriters();
        final Object other$writers = other.getWriters();
        if (this$writers == null ? other$writers != null : !this$writers.equals(other$writers)) return false;
        final Object this$anyReader = this.getAnyReader();
        final Object other$anyReader = other.getAnyReader();
        if (this$anyReader == null ? other$anyReader != null : !this$anyReader.equals(other$anyReader)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AuthorizationsConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $admins = this.getAdmins();
        result = result * PRIME + ($admins == null ? 43 : $admins.hashCode());
        final Object $readers = this.getReaders();
        result = result * PRIME + ($readers == null ? 43 : $readers.hashCode());
        final Object $writers = this.getWriters();
        result = result * PRIME + ($writers == null ? 43 : $writers.hashCode());
        final Object $anyReader = this.getAnyReader();
        result = result * PRIME + ($anyReader == null ? 43 : $anyReader.hashCode());
        return result;
    }

    public String toString() {
        return "AuthorizationsConfig(admins=" + this.getAdmins() + ", readers=" + this.getReaders() + ", writers=" + this.getWriters() + ", anyReader=" + this.getAnyReader() + ")";
    }
}
