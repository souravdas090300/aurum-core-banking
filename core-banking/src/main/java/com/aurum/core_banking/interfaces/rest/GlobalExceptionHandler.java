package com.aurum.core_banking.interfaces.rest;

import com.aurum.core_banking.common.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralised exception handler — converts domain exceptions into consistent
 * RFC 7807 Problem Details JSON responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(AccountNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({AccountNotActiveException.class,
                        InsufficientFundsException.class,
                        CurrencyMismatchException.class,
                        TransactionBlockedException.class})
    public ResponseEntity<Map<String, Object>> handleUnprocessable(RuntimeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a));

        Map<String, Object> body = Map.of(
                "status",    HttpStatus.BAD_REQUEST.value(),
                "error",     "Validation Failed",
                "fields",    fieldErrors,
                "timestamp", Instant.now().toString());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> problem(HttpStatus status, String detail) {
        return ResponseEntity.status(status).body(Map.of(
                "status",    status.value(),
                "error",     status.getReasonPhrase(),
                "detail",    detail,
                "timestamp", Instant.now().toString()));
    }
}
