package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface Listener<T> {

    void accept(final List<T> events) throws IOException, EventAlreadyProcessedException;

    /**
     * A callback for the NakadiReader to check if the listener is still active and ready for consumption of events
     * @return
     */
    default boolean isActive() {
      return true;
    }
}
