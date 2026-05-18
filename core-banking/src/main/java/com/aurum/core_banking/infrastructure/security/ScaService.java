package com.aurum.core_banking.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * PSD2 Article 97 — Strong Customer Authentication (SCA).
 *
 * SCA is required when:
 *  - Transaction amount exceeds €30 (remote electronic payment)
 *  - Payer initiates the payment remotely
 *
 * Two independent factors required:
 *  - Something you KNOW   → Keycloak password
 *  - Something you HAVE   → TOTP / SMS OTP via Keycloak MFA
 *
 * Keycloak handles the actual two-factor flow and sets the ACR claim:
 *  - {@code acr = "silver"} → password only (single factor)
 *  - {@code acr = "gold"}   → MFA completed (two factors — SCA satisfied)
 *
 * This service validates that the SCA claim is present in the JWT for
 * high-value transactions before they are processed.
 */
@Slf4j
@Service
public class ScaService {

    /** PSD2 threshold — €30 for remote electronic payments (Article 97). */
    private static final BigDecimal SCA_THRESHOLD = new BigDecimal("30.00");

    /**
     * Validates SCA was performed for this transaction.
     * Must be called before any transfer exceeding {@code SCA_THRESHOLD}.
     *
     * @param jwtClaims   Keycloak JWT claims map from the incoming request
     * @param amount      transaction amount
     * @param toAccountId destination account (for future first-time payee check)
     * @throws ScaRequiredException if SCA is required but was not performed
     */
    public void validateSca(Map<String, Object> jwtClaims,
                             BigDecimal amount,
                             UUID toAccountId) {

        if (amount.compareTo(SCA_THRESHOLD) <= 0) {
            log.debug("SCA not required — amount {} ≤ threshold {}", amount, SCA_THRESHOLD);
            return;
        }

        // Keycloak sets acr (Authentication Context Class Reference)
        // "gold" means the user completed a second factor (TOTP / SMS OTP)
        String acr = (String) jwtClaims.getOrDefault("acr", "silver");

        if (!"gold".equals(acr)) {
            log.warn("SCA required but not performed — amount={} acr={} toAccount={}",
                    amount, acr, toAccountId);
            throw new ScaRequiredException(
                    "Strong Customer Authentication required for transfers over €" + SCA_THRESHOLD +
                    ". Please re-authenticate with a second factor.");
        }

        log.debug("SCA validated — amount={} acr={}", amount, acr);
    }
}
