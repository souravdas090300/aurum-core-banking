package com.aurum.core_banking.interfaces.rest.controller;

import com.aurum.core_banking.application.service.TransferService;
import com.aurum.core_banking.interfaces.rest.dto.request.TransferRequest;
import com.aurum.core_banking.interfaces.rest.dto.response.TransferResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * Execute a funds transfer.
     * Only TELLER and MANAGER roles may initiate transfers.
     * The Keycloak subject (user ID) is recorded as the performer for audit.
     */
    @PostMapping
    @PreAuthorize("hasRole('TELLER') or hasRole('MANAGER')")
    public ResponseEntity<TransferResponse> createTransfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String performedBy = jwt.getSubject(); // Keycloak user UUID
        TransferResponse result = transferService.transfer(request, performedBy);

        return ResponseEntity
                .created(URI.create("/api/v1/transfers/" + result.transactionId()))
                .body(result);
    }

    /**
     * Retrieve a transfer record by ID.
     * Customers, tellers, managers, and auditors may read transfers.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('TELLER') " +
                  "or hasRole('MANAGER') or hasRole('AUDITOR')")
    public ResponseEntity<TransferResponse> getTransfer(@PathVariable String id) {
        // TODO: implement lookup in a follow-up ticket
        return ResponseEntity.ok().build();
    }
}
