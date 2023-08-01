package org.zalando.spring.boot.fahrschein.nakadi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.fahrschein.NakadiClientBuilder;
import org.zalando.fahrschein.http.api.RequestFactory;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.AbstractConfig;

import java.net.URI;

@Slf4j
class FahrscheinNakadiClientFactory {

    private static final URI INVALID_NAKADI_URL = URI.create("https://nakadi-url.invalid/");

    static NakadiClient create(AbstractConfig config, CursorManager cursorManager, ObjectMapper objectMapper,
            RequestFactory requestFactory) {

        URI nakadiUri = config.getNakadiUrl() != null ? URI.create(config.getNakadiUrl()) : INVALID_NAKADI_URL;
        NakadiClientBuilder ncb = NakadiClient.builder(nakadiUri, requestFactory)
                .withCursorManager(cursorManager).withObjectMapper(objectMapper);

        if (config.getOauth().getEnabled()) {
            ncb = ncb.withAccessTokenProvider(OAuth.buildAccessTokenProvider(config.getOauth()));
        } else {
            log.info("NakadiClient: [{}] - No AccessTokenProvider configured. No 'accessTokenId' was set.",
                    config.getId());
        }

        return ncb.build();
    }
}
