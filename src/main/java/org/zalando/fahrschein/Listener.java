package org.zalando.fahrschein;

import java.util.List;

public interface Listener<T> {
    void onEvent(List<T> event) throws EventProcessingException;

}
