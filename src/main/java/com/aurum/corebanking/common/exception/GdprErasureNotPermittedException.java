package com.aurum.corebanking.common.exception;

public class GdprErasureNotPermittedException extends RuntimeException {
    public GdprErasureNotPermittedException(String message) {
        super(message);
    }
}