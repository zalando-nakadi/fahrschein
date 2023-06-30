package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.Data;

@Data
public class ThreadConfig {

    private int listenerPoolSize = 1;

}
