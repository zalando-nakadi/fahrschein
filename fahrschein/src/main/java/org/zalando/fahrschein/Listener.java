package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface Listener<T> {

    void accept(final List<T> events) throws IOException, EventAlreadyProcessedException;
}
