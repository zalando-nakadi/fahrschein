package org.zalando.fahrschein.example;

@SuppressWarnings("serial")
public class RuntimeEventProcessingException extends RuntimeException {

    public RuntimeEventProcessingException(String message) {
        super(message);
    }

}
