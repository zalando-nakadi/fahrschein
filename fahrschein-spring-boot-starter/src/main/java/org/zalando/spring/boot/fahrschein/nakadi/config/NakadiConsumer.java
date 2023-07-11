package org.zalando.spring.boot.fahrschein.nakadi.config;

import org.zalando.fahrschein.IORunnable;
import org.zalando.spring.boot.fahrschein.nakadi.NakadiListener;
import org.zalando.spring.boot.fahrschein.nakadi.config.properties.ConsumerConfig;

import java.io.IOException;

public interface NakadiConsumer {

    <Type> IORunnable runnable(NakadiListener<Type> listener) throws IOException;

    ConsumerConfig getConsumerConfig();

}