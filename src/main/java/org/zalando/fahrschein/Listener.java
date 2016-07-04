package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.util.List;

public interface Listener<T> {
    void onEvent(final List<T> event) throws IOException, EventAlreadyProcessedException;

    default void onError(final JsonMappingException e) throws JsonMappingException {
        throw e;
    }
}
