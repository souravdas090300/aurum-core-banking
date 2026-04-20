package com.aurum.core_banking.interfaces.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Account management endpoints.
 *
 * RBAC matrix:
 * <pre>
 *   CUSTOMER → read own accounts (ownership check in service layer)
 *   TELLER   → read any account, create accounts
 *   MANAGER  → read + create + freeze accounts
 *   AUDITOR  → read-only, all accounts
 *   ADMIN    → full access including close/delete
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('TELLER') " +
                  "or hasRole('MANAGER') or hasRole('AUDITOR')")
    public ResponseEntity<?> getAccount(@PathVariable UUID id) {
        // TODO: delegate to AccountService in a follow-up ticket
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('TELLER') or hasRole('MANAGER')")
    public ResponseEntity<?> createAccount(@RequestBody Object request) {
        // TODO: delegate to AccountService in a follow-up ticket
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/freeze")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> freezeAccount(@PathVariable UUID id) {
        // TODO: delegate to AccountService in a follow-up ticket
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> closeAccount(@PathVariable UUID id) {
        // TODO: delegate to AccountService in a follow-up ticket
        return ResponseEntity.noContent().build();
    }
}
