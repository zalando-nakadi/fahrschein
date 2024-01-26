package org.zalando.spring.boot.fahrschein.nakadi.config;

import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.ManagedCursorManager;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.AbstractConfig;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.OAuthConfig;

import java.net.URI;

class CursorManagerFactory {

    static CursorManager create(AbstractConfig config, RequestFactory requestFactory) {
        OAuthConfig oauth = config.getOauth();
        if (oauth.getEnabled()) {
            return new ManagedCursorManager(URI.create(config.getNakadiUrl()), requestFactory,
                    OAuth.buildAccessTokenProvider(oauth));
        }

        return new ManagedCursorManager(URI.create(config.getNakadiUrl()), requestFactory);
    }
}
