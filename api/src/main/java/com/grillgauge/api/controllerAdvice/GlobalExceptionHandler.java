package com.grillgauge.api.controllerAdvice;

import com.grillgauge.api.domain.exceptions.ApiKeyGeneratorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the API.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiKeyGeneratorException.class)
    public ResponseEntity<Map<String, Object>> handleApiKeyGeneratorException(ApiKeyGeneratorException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "API Key Generation Failed");
        errorResponse.put("message", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}