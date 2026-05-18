package com.aurum.core_banking.application.service;

/**
 * Thrown when a GDPR erasure request cannot be fulfilled.
 *
 * Common reasons:
 *  - Customer has ACTIVE accounts (must be closed first)
 *  - Within the mandatory 7-year AML/PMLA retention period
 *
 * HTTP mapping: 409 Conflict (see GlobalExceptionHandler).
 */
public class GdprErasureNotPermittedException extends RuntimeException {

    public GdprErasureNotPermittedException(String message) {
        super(message);
    }
}
