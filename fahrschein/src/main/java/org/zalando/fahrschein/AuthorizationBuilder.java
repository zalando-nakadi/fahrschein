package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Authorization;
import org.zalando.fahrschein.domain.Authorization.AuthorizationAttribute;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.zalando.fahrschein.domain.Authorization.AuthorizationAttribute.ANYONE;

/**
 * Use factory method {@link AuthorizationBuilder#authorization()} to create
 * an allow-all authorization object and use {@link #withAdmins} / {@link #withReaders}
 * methods to override admins and/or readers lists.
 */
public class AuthorizationBuilder {

    private final List<AuthorizationAttribute> admins;
    private final List<AuthorizationAttribute> readers;

    private AuthorizationBuilder(List<AuthorizationAttribute> admins, List<AuthorizationAttribute> readers) {
        this.admins = admins;
        this.readers = readers;
    }

    /**
     * No restrictions by default, use {@link #withAdmins} / {@link #withReaders} methods to add them.
     */
    public static AuthorizationBuilder authorization() {
        return new AuthorizationBuilder(emptyList(), emptyList());
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
        List<AuthorizationAttribute> newAdmins = new ArrayList<>(admins.size() + 1);
        newAdmins.addAll(admins);
        newAdmins.add(new AuthorizationAttribute(dataType, value));
        return withAdmins(newAdmins);
    }

    public AuthorizationBuilder addReader(String dataType, String value) {
        List<AuthorizationAttribute> newReaders = new ArrayList<>(readers.size() + 1);
        newReaders.addAll(readers);
        newReaders.add(new AuthorizationAttribute(dataType, value));
        return withReaders(newReaders);
    }

    public Authorization build() {
        List<AuthorizationAttribute> admins = this.admins.isEmpty() ? singletonList(ANYONE) : this.admins;
        List<AuthorizationAttribute> readers = this.readers.isEmpty() ? singletonList(ANYONE) : this.readers;
        return new Authorization(admins, readers);
    }
}
