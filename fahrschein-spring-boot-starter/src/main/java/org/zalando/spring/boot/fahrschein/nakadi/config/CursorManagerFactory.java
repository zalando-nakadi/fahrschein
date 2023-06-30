package org.zalando.spring.boot.fahrschein.nakadi.config;

import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.ManagedCursorManager;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.AbstractConfig;

import java.net.URI;

class CursorManagerFactory {

    static CursorManager create(AbstractConfig config, RequestFactory requestFactory) {
        return new ManagedCursorManager(URI.create(config.getNakadiUrl()), requestFactory,
                OAuth.buildAccessTokenProvider(config.getOauth()));
    }
}
