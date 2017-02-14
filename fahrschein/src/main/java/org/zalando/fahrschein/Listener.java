package org.zalando.fahrschein;

import java.io.IOException;
import java.util.List;

public interface Listener<T> {

    void accept(final List<T> events) throws IOException, EventAlreadyProcessedException;

}
