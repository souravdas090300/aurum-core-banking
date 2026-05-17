package com.aurum.core_banking.interfaces.rest.controller;

import com.aurum.core_banking.application.service.TransferService;
import com.aurum.core_banking.interfaces.rest.dto.request.TransferRequest;
import com.aurum.core_banking.interfaces.rest.dto.response.TransferResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for money transfer operations.
 *
 * <p>Applies rate limiting (see {@link com.aurum.core_banking.infrastructure.security.RateLimitingInterceptor})
 * and Drools fraud screening on every request.
 */
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * Initiate a fund transfer between two accounts.
     *
     * <p>Returns {@code 201 Created} on success, or replays the original result
     * if the {@code idempotencyKey} was already processed (also returns 201).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String performedBy = jwt != null ? jwt.getSubject() : "anonymous";
        TransferResponse response = transferService.execute(request, performedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
