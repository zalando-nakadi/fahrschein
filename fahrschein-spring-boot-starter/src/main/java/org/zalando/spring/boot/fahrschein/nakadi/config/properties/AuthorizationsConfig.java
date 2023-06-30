package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class AuthorizationsConfig {

    @NestedConfigurationProperty
    private AuthorizationUserServiceTeamLists admins = new AuthorizationUserServiceTeamLists();

    @NestedConfigurationProperty
    private AuthorizationUserServiceTeamLists readers = new AuthorizationUserServiceTeamLists();

    @NestedConfigurationProperty
    private AuthorizationUserServiceTeamLists writers = new AuthorizationUserServiceTeamLists();

    private Boolean anyReader = Boolean.FALSE;

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

}
