package org.zalando.fahrschein;

import java.io.IOException;
import java.util.List;

public interface Listener<T> {
    void onEvent(List<T> event) throws IOException, EventAlreadyProcessedException;

}
