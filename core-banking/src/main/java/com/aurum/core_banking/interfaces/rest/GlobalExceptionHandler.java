package com.aurum.core_banking.interfaces.rest;

import com.aurum.core_banking.common.exception.AccountNotActiveException;
import com.aurum.core_banking.common.exception.AccountNotFoundException;
import com.aurum.core_banking.common.exception.CurrencyMismatchException;
import com.aurum.core_banking.common.exception.InsufficientFundsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    ProblemDetail handleNotFound(AccountNotFoundException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Account Not Found");
        return pd;
    }

    @ExceptionHandler(InsufficientFundsException.class)
    ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle("Insufficient Funds");
        pd.setProperty("errorCode", "INSUFFICIENT_FUNDS");
        return pd;
    }

    @ExceptionHandler(AccountNotActiveException.class)
    ProblemDetail handleNotActive(AccountNotActiveException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Account Not Active");
        pd.setProperty("errorCode", "ACCOUNT_NOT_ACTIVE");
        return pd;
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    ProblemDetail handleCurrencyMismatch(CurrencyMismatchException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle("Currency Mismatch");
        pd.setProperty("errorCode", "CURRENCY_MISMATCH");
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setTitle("Bad Request");
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList());
        return pd;
    }

    // Catch-all — never leak internal details to the client
    @ExceptionHandler(Exception.class)
    ProblemDetail handleGeneric(Exception ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please contact support.");
        pd.setTitle("Internal Server Error");
        return pd;
    }
}
