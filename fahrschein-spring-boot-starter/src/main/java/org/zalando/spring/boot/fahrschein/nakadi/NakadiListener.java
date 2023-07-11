package org.zalando.spring.boot.fahrschein.nakadi;

import org.zalando.fahrschein.Listener;

public interface NakadiListener<T> extends Listener<T> {

    public Class<T> getEventType();

}
