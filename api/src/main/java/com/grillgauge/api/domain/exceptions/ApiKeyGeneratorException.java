package com.grillgauge.api.domain.exceptions;

public class ApiKeyGeneratorException extends RuntimeException {
    public ApiKeyGeneratorException(String message) {
        super(message);
    }

    public ApiKeyGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
