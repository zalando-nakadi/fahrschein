package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Authorization;
import org.zalando.fahrschein.domain.Authorization.AuthorizationAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

public class AuthorizationBuilder {

    private final List<AuthorizationAttribute> admins;
    private final List<AuthorizationAttribute> readers;

    private AuthorizationBuilder() {
        admins = null;
        readers = null;
    }

    private AuthorizationBuilder(List<AuthorizationAttribute> admins, List<AuthorizationAttribute> readers) {
        this.admins = admins;
        this.readers = readers;
    }

    public static AuthorizationBuilder authorization() {
        return new AuthorizationBuilder();
    }

    public static Authorization notRestricted() {
        return authorization().withAdmins(AuthorizationAttribute.ANYONE).withReaders(AuthorizationAttribute.ANYONE).build();
    }

    public AuthorizationBuilder withAdmins(List<AuthorizationAttribute> admins) {
        return new AuthorizationBuilder(admins, readers);
    }

    public AuthorizationBuilder withReaders(List<AuthorizationAttribute> readers) {
        return new AuthorizationBuilder(admins, readers);
    }

    public AuthorizationBuilder withAdmins(AuthorizationAttribute... admins) {
        return new AuthorizationBuilder(asList(admins), readers);
    }

    public AuthorizationBuilder withReaders(AuthorizationAttribute... readers) {
        return new AuthorizationBuilder(admins, asList(readers));
    }

    public AuthorizationBuilder addAdmin(String dataType, String value) {
        ArrayList<AuthorizationAttribute> newAdmins = Optional.ofNullable(admins).map(ArrayList::new).orElse(new ArrayList<>());
        newAdmins.add(new AuthorizationAttribute(dataType, value));
        return withAdmins(newAdmins);
    }

    public AuthorizationBuilder addReader(String dataType, String value) {
        ArrayList<AuthorizationAttribute> newReaders = Optional.ofNullable(readers).map(ArrayList::new).orElse(new ArrayList<>());
        newReaders.add(new AuthorizationAttribute(dataType, value));
        return withReaders(newReaders);
    }

    public Authorization build() {
        return new Authorization(admins, readers);
    }
}
