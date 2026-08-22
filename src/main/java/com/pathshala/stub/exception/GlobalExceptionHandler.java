package com.pathshala.stub.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Centralised error response shaping for all controllers.
 * Keeps controllers clean — they only throw, never build error ResponseEntities.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Validation failures from @Valid — returns field-level error map. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(Map.of("errors", fieldErrors));
    }

    /**
     * DB unique-constraint violations — specifically the phone_number uniqueness rule.
     * Returns 409 Conflict with a clear message instead of a generic 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleIntegrityViolation(
            DataIntegrityViolationException ex) {
        String rootMsg = ex.getMostSpecificCause().getMessage();
        String userFacingMsg = (rootMsg != null && rootMsg.contains("phone_number"))
                ? "Phone number is already in use by another user"
                : "A database constraint was violated: " + rootMsg;
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", userFacingMsg));
    }

    /** Business-rule violations (map link can't be parsed, paathshaala has users, etc.) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    /** 404 — entity not found. Thrown explicitly in services via orElseThrow. */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
