package org.zalando.fahrschein.domain;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;

@Immutable
public final class Authorization {

    @Immutable
    public static final class AuthorizationAttribute {

        public static final AuthorizationAttribute ANYONE = new AuthorizationAttribute("*", "*");

        private final String dataType;
        private final String value;

        public AuthorizationAttribute(String dataType, String value) {
            this.dataType = dataType;
            this.value = value;
        }

        public String getDataType() {
            return dataType;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof AuthorizationAttribute))
                return false;
            AuthorizationAttribute that = (AuthorizationAttribute) o;
            return dataType.equals(that.dataType) &&
                    value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dataType, value);
        }
    }

    private final List<AuthorizationAttribute> admins;
    private final List<AuthorizationAttribute> readers;

    public Authorization(List<AuthorizationAttribute> admins, List<AuthorizationAttribute> readers) {
        this.admins = unmodifiableList(admins);
        this.readers = unmodifiableList(readers);
    }

    public List<AuthorizationAttribute> getAdmins() {
        return admins;
    }

    public List<AuthorizationAttribute> getReaders() {
        return readers;
    }
}
