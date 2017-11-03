package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.util.List;

public interface EventReader<T> {
    List<T> read(JsonParser jsonParser) throws IOException;
}
