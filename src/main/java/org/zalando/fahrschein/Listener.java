package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.util.List;

public interface Listener<T> {

    void accept(final List<T> events) throws IOException, EventAlreadyProcessedException;

    default void onMappingException(final JsonMappingException e) throws JsonMappingException {
        throw e;
    }
}
