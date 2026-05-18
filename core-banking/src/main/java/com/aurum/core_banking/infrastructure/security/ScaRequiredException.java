package com.aurum.core_banking.infrastructure.security;

/**
 * Thrown when a high-value transfer is attempted without completing
 * Strong Customer Authentication (PSD2 Article 97).
 *
 * HTTP mapping: 403 Forbidden (see GlobalExceptionHandler).
 */
public class ScaRequiredException extends RuntimeException {

    public ScaRequiredException(String message) {
        super(message);
    }
}
