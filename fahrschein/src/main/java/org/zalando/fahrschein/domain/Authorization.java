package org.zalando.fahrschein.domain;

import javax.annotation.concurrent.Immutable;
import java.util.List;

@Immutable
public class Authorization {

    @Immutable
    public static class AuthorizationAttribute {

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
    }

    private final List<AuthorizationAttribute> admins;
    private final List<AuthorizationAttribute> readers;

    public Authorization(List<AuthorizationAttribute> admins, List<AuthorizationAttribute> readers) {
        this.admins = admins;
        this.readers = readers;
    }

    public List<AuthorizationAttribute> getAdmins() {
        return admins;
    }

    public List<AuthorizationAttribute> getReaders() {
        return readers;
    }
}
