package com.grillgauge.api.domain.exceptions;

/** Exception thrown when there is an error generating an API key. */
public class ApiKeyGeneratorException extends RuntimeException {
  public ApiKeyGeneratorException(String message) {
    super(message);
  }

  public ApiKeyGeneratorException(String message, Throwable cause) {
    super(message, cause);
  }
}
