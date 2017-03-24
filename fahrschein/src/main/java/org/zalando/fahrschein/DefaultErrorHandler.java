package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;

public class DefaultErrorHandler implements ErrorHandler {
    public static final DefaultErrorHandler INSTANCE = new DefaultErrorHandler();

    private DefaultErrorHandler() {

    }

    @Override
    public void onMappingException(JsonMappingException exception) throws IOException {
        throw exception;
    }
}
