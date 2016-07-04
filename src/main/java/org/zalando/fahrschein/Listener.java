package org.zalando.fahrschein;

import java.io.IOException;
import java.util.List;

public interface Listener<T> {
    void onEvent(final List<T> event) throws IOException, EventAlreadyProcessedException;

    default void onError(final IOException e) throws IOException {
        // no operation
    }
}
