package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;

public interface ErrorHandler {
    void onMappingException(JsonMappingException exception) throws IOException;
}
